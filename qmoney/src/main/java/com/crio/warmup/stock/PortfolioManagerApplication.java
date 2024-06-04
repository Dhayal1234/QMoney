
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedReader;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class PortfolioManagerApplication {


  private static final String TOKEN = "f71d2aff7eda45260540e474df4faa986f285d6d"; // Replace with your actual token

  public static String getToken() {
    return TOKEN;
  }

  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if (candles == null || candles.isEmpty()) {
      return null;
    }
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    if (candles == null || candles.isEmpty()) {
      return null;
    }
    return candles.get(candles.size() - 1).getClose();
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {


         List<PortfolioTrade> portfolioTrades = getObjectMapper().readValue(
        resolveFileFromResources(filename), new TypeReference<List<PortfolioTrade>>() {});
         return portfolioTrades;


      }




  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String tiingoRestURL = prepareUrl(trade, endDate, token);
    TiingoCandle[] tiingoCandleArray =
        restTemplate.getForObject(tiingoRestURL, TiingoCandle[].class);
    return Arrays.stream(tiingoCandleArray).collect(Collectors.toList());
  }


  
  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(file, PortfolioTrade[].class));
    LocalDate endDate = LocalDate.parse(args[1]);

    String token = getToken();
    RestTemplate restTemplate = new RestTemplate();

    return trades.stream()
        .map(trade -> {
          try {
            List<Candle> candles = fetchCandles(trade, endDate, token);
            Double buyPrice = getOpeningPriceOnStartDate(candles);
            Double sellPrice = getClosingPriceOnEndDate(candles);
            return calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .sorted(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed())
        .collect(Collectors.toList());
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    LocalDate startDate = trade.getPurchaseDate();
    double totalReturn = (sellPrice - buyPrice) / buyPrice;
    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
    double yearsBetween = (double) daysBetween / 365.24;
    double annualizedReturn = Math.pow((1 + totalReturn), (1 / yearsBetween)) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturn, totalReturn);
  }





  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.






  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    String filename = args[0]; // Assuming args[0] contains the filename
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    List<Trade> trades = Arrays.asList(objectMapper.readValue(file, Trade[].class));

    List<String> symbols = trades.stream()
            .map(Trade::getSymbol)
            .collect(Collectors.toList());

    return symbols;
}
public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
  File file = resolveFileFromResources(args[0]);
  ObjectMapper objectMapper = getObjectMapper();
  List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(file, PortfolioTrade[].class));
  LocalDate endDate = LocalDate.parse(args[1]);

  String token = "f71d2aff7eda45260540e474df4faa986f285d6d"; // Replace with your Tiingo API key
  RestTemplate restTemplate = new RestTemplate();

  List<PortfolioTrade> sortedTrades = trades.stream()
      .map(trade -> {
        String url = prepareUrl(trade, endDate, token);
        TiingoCandle[] candles = restTemplate.getForObject(url, TiingoCandle[].class);
        if (candles != null) {
          trade.setClosingPrice(candles[candles.length - 1].getClose());
        } 
        return trade;
      })
      .sorted(Comparator.comparingDouble(PortfolioTrade::getClosingPrice))
      .collect(Collectors.toList());

  return sortedTrades.stream().map(PortfolioTrade::getSymbol).collect(Collectors.toList());
}
/*
public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
  final String tiingoToken = "953e5d1702c35f1aabd7a475fd8d256e2274d731";
  List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
  LocalDate endDate = LocalDate.parse(args[1]);
  RestTemplate restTemplate = new RestTemplate();
  List<TotalReturnsDto> totalReturnsDtos = new ArrayList<>();
  List<String> listOfSortSymbolsOnClosingPrice = new ArrayList<>();
  for (PortfolioTrade portfolioTrade : portfolioTrades) {
    String tiingoURL = prepareUrl(portfolioTrade, endDate, tiingoToken);
    TiingoCandle[] tiingoCandleArray = restTemplate.getForObject(tiingoURL, TiingoCandle[].class);
    totalReturnsDtos.add(new TotalReturnsDto(portfolioTrade.getSymbol(),
        tiingoCandleArray[tiingoCandleArray.length - 1].getClose()));
  }
  Collections.sort(totalReturnsDtos,
      (a, b) -> Double.compare(a.getClosingPrice(), b.getClosingPrice()));
  for (TotalReturnsDto totalReturnsDto : totalReturnsDtos) {
    listOfSortSymbolsOnClosingPrice.add(totalReturnsDto.getSymbol());
  }
  return listOfSortSymbolsOnClosingPrice;
}
*/



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
     return Paths.get(
         Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();

  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }



  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //    expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "resolveFileFromResources(valueOfArgument0).toString()";
     String toStringOfObjectMapper = "getObjectMapper().toString()";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "24";

     

     try {
      resultOfResolveFilePathArgs0 = resolveFileFromResources(valueOfArgument0).toString(); // 2. Evaluate resolveFileFromResources(args[0])
      toStringOfObjectMapper = getObjectMapper().toString(); // 3. Evaluate getObjectMapper().toString()
  } catch (URISyntaxException e) {
      e.printStackTrace();
  }


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args) {
    try {
      // Check for correct argument length
      if (args.length < 3) {
        System.err.println("Usage: <file> <end-date> <token>");
        return Collections.emptyList();
      }

      // Parse the arguments
      String filename = args[0];
      LocalDate endDate = LocalDate.parse(args[1]);
      String token = args[2];

      // Read trades from the JSON file
      ObjectMapper objectMapper = new ObjectMapper();
      List<PortfolioTrade> trades = objectMapper.readValue(new File(filename), new TypeReference<List<PortfolioTrade>>() {});

      // Create an instance of RestTemplate
      RestTemplate restTemplate = new RestTemplate();

      // Get an instance of PortfolioManager from PortfolioManagerFactory
      PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

      // Calculate the annualized returns using the PortfolioManager instance
      return portfolioManager.calculateAnnualizedReturn(trades, endDate);

    } catch (IOException e) {
      System.err.println("Error reading the JSON file: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
    }

    return Collections.emptyList();
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

  
    printJsonObject(mainCalculateSingleReturn(args));

    List<AnnualizedReturn> returns = mainCalculateReturnsAfterRefactor(args);


  }
public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
  String symbol = trade.getSymbol();
  String startDate = trade.getPurchaseDate().toString(); // Use the purchase date as the start date
  String endDateStr = endDate.toString();
  return String.format("https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s",
      symbol, startDate, endDateStr, token);
}

}

