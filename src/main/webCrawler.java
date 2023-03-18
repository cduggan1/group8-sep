package main;

import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class webCrawler {

    public static String Daft(String parentUrl, String BER_Query) {

        boolean endOfList = false;
        boolean firstProperty = true;

        int index = 0;

        String BER_Ratings[] = {"G","F","E2","E1","D2","D1","C3","C2","C1","B3","B2","B1","A3","A2","A1"};

        String tempUrl = parentUrl + index;
        String json = "{ \"Residences\":[{";
        StringBuffer json_sb= new StringBuffer(json);

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
                    parentUrl = parentUrl + index;
                }
            }
            System.out.println(urlList);

            //Scrapy Bit
            for (String url : urlList) {

                Document document = Jsoup.connect(url).get();
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

                    System.out.println(title.text() + "\n" + price.text());
                    if (!firstProperty) {
                        json_sb.append("{\"title\":\"" + title.text() + "\",\"price\":\"" + price.text() + "\",");
                    }
                    else {
                        json_sb.append("\"title\":\"" + title.text() + "\",\"price\":\"" + price.text() + "\",");
                        firstProperty = false;
                    }
                    if (BER != null) {
                        json_sb.append("\"BER\":\"" + BER.attr("alt") + "\",");
                        System.out.println(BER.attr("alt"));
                    }
                    if (type != null) {
                        json_sb.append("\"type\":\"" + type.text() + "\",");
                        System.out.println(type.text());
                    }
                    if (lease != null) {
                        json_sb.append("\"lease\":\"" + lease.text() + "\",");
                        System.out.println(lease.text());
                    }
                    if (beds != null) {
                        json_sb.append("\"beds\":\"" + beds.text() + "\",");
                        System.out.println(beds.text());
                    }
                    if (baths != null) {
                        json_sb.append("\"baths\":\"" + baths.text() + "\",");
                        System.out.println(baths.text());
                    }

                    final Elements facilities = document.select("ul [class*=PropertyDetails]");
                    for (Element li : facilities) {
                        json_sb.append("\"" + li.select("li").text() + "\":\"" + li.select("li").text()
                                + "\",");

                        System.out.println(li.select("li").text());
                    }

                    json_sb.deleteCharAt(json_sb.length()-1);
                    json_sb.append("},");
                    System.out.println("");
                }
            }

            json_sb.deleteCharAt(json_sb.length()-1);
            json = json_sb.toString();
            return json + "]}";
        }

        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}