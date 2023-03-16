package main;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class webCrawler {

    public static void webCrawler(String parentURL, String BER_Query) {

    //public static void main(String[] args) {

        boolean endOfList = false;

        int index = 0;
        int count = 0;

        String parentUrl = "https://www.daft.ie/property-for-rent/dublin-city-centre-dublin?furnishing=furnished" +
                "&pageSize=20&from=" + index;

        try {
            ArrayList<String> urlList = new ArrayList<>();

            //Crawly Bit
            while (!endOfList) {

                Document urlDoc =  Jsoup.connect(parentUrl).get();
                Elements links = urlDoc.select("[href*=/for-rent/]");

                for (Element link : links) {
                    if (link.attr("abs:href").contains("/for-rent/")) {
                        //System.out.println(link.attr("abs:href"));
                        urlList.add(link.attr("abs:href"));
                    }
                }
                if (!links.attr("abs:href").contains("/for-rent/")) {
                    endOfList = true;
                }
                else {
                    index = index + 20;
                    parentUrl = "https://www.daft.ie/property-for-rent/dublin-city-centre-dublin?pageSize=20&from=" + index;
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

              //  if (BER.attr("alt").equalsIgnoreCase(BER_Query) && count <= 5 && type != null) {
                    System.out.println(title.text() + "\n" + price.text());

                    if (BER != null) {
                        System.out.println(BER.attr("alt"));
                    }
                    if (type != null) {
                        System.out.println(type.text());
                    }
                    if (lease != null) {
                        System.out.println(lease.text());
                    }
                    if (beds != null) {
                        System.out.println(beds.text());
                    }
                    if (baths != null) {
                        System.out.println(baths.text());
                    }

                    final Elements facilities = document.select("ul [class*=PropertyDetails]");
                    for (Element li : facilities) {
                        System.out.println(li.select("li").text());
                    }
                    System.out.println("");
                    count++;

                }
           // }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
