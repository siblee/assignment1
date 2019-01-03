package redhat.com.assignment1.Model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * POJO class for Transactions.
 */

public class Transaction {

    @JsonFormat(pattern = "dd-MM-yyyy")
    Date date;
    String type;
    double amount;

    public Transaction() {
    }

    public Transaction(Date date, String type, double amount) {
        this.date = date;
        this.type = type;
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                '}';
    }

    public String toCSVLine() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date) + "_" + type + "," + dateFormat.format(date) + "," + type + "," + amount + "\n";
    }

}
