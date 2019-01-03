package redhat.com.assignment1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import redhat.com.assignment1.Model.Transaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class contains the functions to handle a GET and a POST call to display and store transactions respectively.
 */

@SpringBootApplication
@RestController
public class Assignment1Application {

	public static void main(String[] args) {
		SpringApplication.run(Assignment1Application.class, args);
	}

	/**
	 * This function is getting values from the file data.txt that is used for storing the data.
	 * The function has no parameters.
	 * @return return a map containing all the transactions stored before.
	 */
	private Map<String, Transaction> getDataFromFile() {
		Map<String, Transaction> transactions = new HashMap<>();
		Path path = Paths.get("data.txt");
		try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
			// parsing csv lines and converting records into Transaction objects.
			for (Object obj: reader.lines().toArray()){
				String line = (String) obj;
				String[] values = line.split(",");
				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				String key = values[0];
				Date date = null;
				try {
					date = dateFormat.parse(values[1]);
				} catch (Exception es) {
					System.out.println("Date could not be parsed" + es.toString());
				}
				String type = values[2];
				double amount = Double.parseDouble(values[3]);
				Transaction transaction = new Transaction(date, type, amount);
				transactions.put(key, transaction);
			}

		}catch (IOException ex) {
			ex.printStackTrace();
		}
		return transactions;
	}

	/**
	 * The function is for the response of REST GET (/getdata).
	 * @return a JSON containing all the stored data.
	 */
	@RequestMapping(value = "/getdata")
	public ResponseEntity<Object> showData() {
		Map<String, Transaction> transactions = getDataFromFile();
		return new ResponseEntity<>(transactions.values(), HttpStatus.OK);
	}

	/**
	 * The function is for the response of REST GET (/getdata/<type>).
	 * @return a JSON containing the stored data of type <type>.
	 */
	@RequestMapping(value = "/getdata/{type}")
	public ResponseEntity<Object> showData(@PathVariable String type) {
		Map<String, Transaction> transactions = getDataFromFile();
		Map<String, Transaction> transactionsOfType = new HashMap<>();
		for (Map.Entry <String, Transaction> transaction: transactions.entrySet()) {
			if (transaction.getValue().getType().equals(type)) {
				transactionsOfType.put(transaction.getKey(), transaction.getValue());
			}
		}
		return new ResponseEntity<>(transactionsOfType.values(), HttpStatus.OK);
	}

	/**
	 * The function store the transactions in data.txt file.
	 * @param transactions a Map containing previous and present transactions.
	 */
	private void saveData(Map<String, Transaction> transactions) {
		Path path = Paths.get("data.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))){
			for (Transaction transaction : transactions.values()){
				writer.write(transaction.toCSVLine());
			}

		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	/**
	 * This function is for the REST POST call that saves the transactions.
	 * @param input a json payload containing the transactions.
	 * @return acknowledgement string as a response to the REST call.
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ResponseEntity<Object> saveTransaction(@RequestBody String input) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			// parsing the payload and converting them as Transaction Object.
			Transaction[] transactions = mapper.readValue(input, Transaction[].class);
			// Retrieving all existing transactions.
			Map<String, Transaction> allTransactions = getDataFromFile();
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			for (Transaction transaction: transactions){
				String key = dateFormat.format(transaction.getDate())+'_'+transaction.getType();
				// checking whether type or date string contains any comma as data will be stored as csv.
				if (key.contains(",")) {
					return new ResponseEntity<>("Transaction is not saved as comma " +
							"',' is not allowed as a value in Date or type field." , HttpStatus.EXPECTATION_FAILED);
				}
				// Checking whether transaction exists with same date and type.
				if (allTransactions.containsKey(key)) {
					// if exists then adding the amount of new corresponding transaction to the existing one.
					Transaction t = allTransactions.get(key);
					t.setAmount(t.getAmount()+transaction.getAmount());
					allTransactions.put(key, t);
				}
				else {
					// appending transactions with different date or type to the existing one.
					allTransactions.put(key, transaction);
				}
			}
			// saving the updated transaction records.
			saveData(allTransactions);
		} catch (Exception es) {
			return new ResponseEntity<>("Transaction is not saved. Reason is as follows:\n" + es.toString(),
					HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>("Transaction is saved successfully", HttpStatus.CREATED);
	}

}
