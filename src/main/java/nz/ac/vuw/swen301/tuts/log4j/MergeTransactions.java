package nz.ac.vuw.swen301.tuts.log4j;

import org.apache.log4j.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MergeTransactions {
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());

	private static final Logger fileLogger = Logger.getLogger("FILE");
	private static final Logger transactionLogger = Logger.getLogger("TRANSACTIONS");

	public static void main(String[] args) {
		// 初始化日志配置
		BasicConfigurator.configure();

		List<Purchase> transactions = new ArrayList<Purchase>();
		readData("transactions1.csv", transactions);
		readData("transactions2.csv", transactions);
		readData("transactions3.csv", transactions);
		// readData("transactions4.csv", transactions);

		transactionLogger.info(transactions.size() + " transactions imported");
		transactionLogger.info("total value: " + CURRENCY_FORMAT.format(computeTotalValue(transactions)));
		transactionLogger.info("max value: " + CURRENCY_FORMAT.format(computeMaxValue(transactions)));
	}
	private static double computeTotalValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p : transactions) {
			v = v + p.getAmount();
		}
		return v;
	}

	private static double computeMaxValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p : transactions) {
			v = Math.max(v, p.getAmount());
		}
		return v;
	}

	// 从文件读取交易数据，并添加到列表中
	private static void readData(String fileName, List<Purchase> transactions) {
		File file = new File(fileName);
		String line = null;
		// 打印信息给用户
		fileLogger.info("import data from " + fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				Purchase purchase = new Purchase(
						values[0],
						Double.parseDouble(values[1]),
						DATE_FORMAT.parse(values[2])
				);
				transactions.add(purchase);
				// 仅用于调试
				transactionLogger.debug("imported transaction " + purchase);
			}
		} catch (FileNotFoundException x) {
			// 打印警告
			fileLogger.warn("file " + fileName + " does not exist - skip", x);
		} catch (IOException x) {
			// 打印错误信息和详情
			fileLogger.error("problem reading file " + fileName, x);
		} catch (ParseException x) {
			// 打印错误信息和详情
			fileLogger.error("cannot parse date from string - please check whether syntax is correct: " + line, x);
		} catch (NumberFormatException x) {
			// 打印错误信息和详情
			fileLogger.error("cannot parse double from string - please check whether syntax is correct: " + line, x);
		} catch (Exception x) {
			// 其他异常
			fileLogger.error("exception reading data from file " + fileName + ", line: " + line, x);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				// 打印错误信息和详情
				fileLogger.error("cannot close reader used to access " + fileName, e);
			}
		}
	}
	static {
		// FILE 日志记录器配置
        try {
            fileLogger.addAppender(new FileAppender(new SimpleLayout(), "logs.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileLogger.addAppender(new ConsoleAppender(new SimpleLayout()));

		// TRANSACTIONS 日志记录器配置
		transactionLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
	}
}
