package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class webCrawler implements Callable {

    public static String BER_Query;
    public static HashMap<String, String> urlMap = new HashMap<String, String>();
    public webCrawler (String Url, String BER_Query, int index){

        this.BER_Query = BER_Query;
        urlMap.put(Integer.toString(index), Url);
    }

    public static String Daft(String parentUrl, String BER_Query) {

        int index = 0;
        int crawlerIndex = 0;

        ArrayList<String> Pages = daftGetUrlList(parentUrl);
        ArrayList<webCrawler> Crawlers = new ArrayList<>();
        ArrayList<Thread> Threads = new ArrayList<>();

        String json = "{\"Residences\":[";

        ArrayList<FutureTask> pageTasks = new ArrayList<>();

        for (String page : Pages) {
            Crawlers.add(new webCrawler(page, BER_Query, crawlerIndex));
            crawlerIndex++;

            pageTasks.add(new FutureTask(Crawlers.get(Crawlers.size()-1)));

            Threads.add(new Thread(pageTasks.get(pageTasks.size()-1), Integer.toString(index)));
            Threads.get(Threads.size()-1).start();
            index++;
        }

        for (FutureTask task : pageTasks) {
            try {
                json = json + task.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        StringBuilder json_sb= new StringBuilder(json);
        if (json.contains("title")) {
            json_sb.deleteCharAt(json_sb.length() - 1);
        } else {
            json_sb.append("{}");
        }
        json = json_sb.toString();
        return json + "]}";
    }


    public static ArrayList<String> daftGetUrlList(String parentUrl) {

        int index = 0;

        boolean endOfList = false;

        String tempUrl = parentUrl;

        try {
            ArrayList<String> urlList = new ArrayList<>();

            //Crawly Bit
            while (!endOfList) {

                Document urlDoc =  Jsoup.connect(parentUrl).get();
                Elements links = urlDoc.select("[href*=/for-rent/]");

                for (Element link : links) {
                    if (link.attr("abs:href").contains("/for-rent/")) {
                        urlList.add(link.attr("abs:href"));
                    }
                }
                if (!links.attr("abs:href").contains("/for-rent/")) {
                    endOfList = true;
                }
                else {
                    index = index + 20;
                    parentUrl = tempUrl + index;
                }
            }
                System.out.println(urlList);
                return urlList;
            } catch (Exception e) {
                e.printStackTrace();
            }

        return null;
    }

        public static String daftScrape(String BER_Query) {

        System.out.println("Thread Number " + Thread.currentThread().getName() + " Started");

        ArrayList<ArrayList<String>> BackupBER = new ArrayList<ArrayList<String>>(16);
        int count = 0;

        String BER_Ratings[] = {"G","F","E2","E1","D2","D1","C3","C2","C1","B3","B2","B1","A3","A2","A1"};

        String json = "";
        StringBuilder json_sb= new StringBuilder(json);

        try {
                Document document = Jsoup.connect(urlMap.get(Thread.currentThread().getName())).get();
                Element title = document.selectFirst("h1[class*=TitleBlock]");
                Element price = document.selectFirst("p:contains(€)");
                Element BER = document.selectFirst("img[class*=BerDetails]");
                Element type = document.selectFirst("p[data-testid$=property-type]");
                Element lease = document.selectFirst("li:contains(Minimum)");
                Element beds = document.selectFirst("li:contains(Bedroom)");
                Element baths = document.selectFirst("li:contains(Bath)");

                if ((Arrays.asList(BER_Ratings).indexOf(BER_Query)
                        <= Arrays.asList(BER_Ratings).indexOf(BER.attr("alt"))
                            || BER_Query.equalsIgnoreCase("All")) && type != null) {

                    //System.out.println(title.text() + "\n" + price.text());
                    if (title != null && price != null) {
                        json_sb.append("{\"title\":\"" + title.text() + "\",\"price\":\"" + price.text() + "\",");
                    }
                    if (BER != null) {
                        json_sb.append("\"BER\":\"" + BER.attr("alt") + "\",");
                        //System.out.println(BER.attr("alt"));
                    }
                    if (type != null) {
                        json_sb.append("\"type\":\"" + type.text() + "\",");
                        //System.out.println(type.text());
                    }
                    if (lease != null) {
                        json_sb.append("\"lease\":\"" + lease.text() + "\",");
                        //System.out.println(lease.text());
                    }
                    if (beds != null) {
                        json_sb.append("\"beds\":\"" + beds.text() + "\",");
                        //System.out.println(beds.text());
                    }
                    if (baths != null) {
                        json_sb.append("\"baths\":\"" + baths.text() + "\",");
                        //System.out.println(baths.text());
                    }

                    Elements facilities = document.select("ul [class*=PropertyDetails]");
                    for (Element li : facilities) {
                        json_sb.append("\"" + li.select("li").text() + "\":\"" + li.select("li").text()
                                + "\",");

                        //System.out.println(li.select("li").text());
                    }

                    json_sb.deleteCharAt(json_sb.length()-1);
                    json_sb.append("},");
                    //System.out.println("");
            }

            json = json_sb.toString();
            System.out.println("Thread Number " + Thread.currentThread().getName() + " Finished");
            return json;
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object call() throws Exception {
        String threadJson = daftScrape(BER_Query);
        return threadJson;
    }
}
