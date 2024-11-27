package com.osslot.educorder.infrastructure.activities.repository;

import com.osslot.educorder.domain.activities.model.Order;
import com.osslot.educorder.domain.activities.repository.OrderRepository;
import com.osslot.educorder.infrastructure.activities.repository.abby.Billing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AbbyOrderRepository implements OrderRepository {

  private final RestClient abbyRestClient;
  private final String apiKey;

  public AbbyOrderRepository(RestClient abbyRestClient, @Value("${abby.api.key}") String apiKey) {
    this.abbyRestClient = abbyRestClient;
    this.apiKey = apiKey;
  }

  @Override
  public Order add(Order order) {
    return null;
  }

  public String getOrders() {
    return abbyRestClient
        .get()
        .uri(
            "https://app.abby.fr/api/v2/billings?page=1&type[]=invoice&type[]=advance&limit=20&archived=false")
        .header("accept", "application/json, text/plain, */*")
        .header("authorization", "Bearer " + apiKey)
        .header("content-type", "application/json")
        .retrieve()
        .body(String.class);
  }

  public Billing getOrder(String id) {
    return abbyRestClient
        .get()
        .uri("https://app.abby.fr/api/billing/65b27c5309295be5897316e7")
        .header("accept", "application/json, text/plain, */*")
        .header("authorization", "Bearer " + apiKey)
        .header("content-type", "application/json")
        .header("referer", "https://app.abby.fr/billing/documents/invoices")
        .header("authority", "app.abby.fr")
        .retrieve()
        .body(Billing.class);
  }

  public Object createOrder(Billing billing) {
    var result =
        abbyRestClient
            .post()
            .uri("https://app.abby.fr/api/billing")
            .header("accept", "application/json, text/plain, */*")
            .header("authorization", "Bearer " + apiKey)
            .header("content-type", "application/json")
            .header("referer", "https://app.abby.fr/billing/documents/invoices")
            .header("authority", "app.abby.fr")
            .body(billing)
            .retrieve()
            .body(Object.class);
    return result;
  }

  //    curl 'https://app.abby.fr/api/billing/6581f9d1502efc89dd7c29a4' \
  //            -H 'authority: app.abby.fr' \
  //            -H 'accept: application/json, text/plain, */*' \
  //            -H 'accept-language: en-GB,en;q=0.8' \
  //            -H 'authorization: Bearer
  // eyJhbGciOiJSUzI1NiIsImtpZCI6IjViNjAyZTBjYTFmNDdhOGViZmQxMTYwNGQ5Y2JmMDZmNGQ0NWY4MmIiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQXVkcmV5IFRvc3NlciIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQWNIVHRkOUNOWmxFeVNJQmFCcVJ5Yy0tVEF5ZHhJT3hnOVNFRWJQMGR4U2owbE09czk2LWMiLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYXBwLWFiYnktcHJvZHVjdGlvbiIsImF1ZCI6ImFwcC1hYmJ5LXByb2R1Y3Rpb24iLCJhdXRoX3RpbWUiOjE3MDMxMDAwNTMsInVzZXJfaWQiOiI2TU9TRzJPdzNiTUxWWm52eWZlR21va0xwYmwyIiwic3ViIjoiNk1PU0cyT3czYk1MVlpudnlmZUdtb2tMcGJsMiIsImlhdCI6MTcwNjE4ODEwMiwiZXhwIjoxNzA2MTkxNzAyLCJlbWFpbCI6ImF0b3NzZXJ0c2FAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZ29vZ2xlLmNvbSI6WyIxMTE3Mjk5MDk1MTY2NDM1NDk5ODciXSwiZW1haWwiOlsiYXRvc3NlcnRzYUBnbWFpbC5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJnb29nbGUuY29tIn19.Nw95UW1cOnHEUh4PN7cbi2uCQ21hjmltsH__7IZlJVm7bCoR4B8A5vOP1c3s1T6hJVGAE5me9SlkL2wgdU-0mZUZgidU10oIF7EQWxs2vKMWIZXTWjLkXVDhrhhRhBYVJJz4OnuzDAn9Mds41Hjy7mleX-jFXZQI6ThckzD1naMwIUtTKgxIQ1cHcvD-vVzixAcej9DOFU_689tZRvcCEeB91z8j6gqQGtr37ysD9Z4SQ7o-LLmqCri2k9AyTrssgD8F-GiBgcb7MEakNGz3dfyiGvnHuG7_SchbMcRQMYAZ0mdGTQQm3VU06R9c-iQY316DTxiFHZblK35ej2-sag' \
  //            -H 'cookie:
  // FPID=FPID2.2.QY37Wt9UkJMggoBmEjSbLnauPTBAloe1NmKXGztg%2FCU%3D.1696067183;
  // FPAU=1.2.1131253277.1705140153; ay_anonymous_id=78322229-4a77-499e-aa2d-d41dd9bd9954;
  // i18n_redirected=en; _ga=GA1.1.1740730560.1706184330;
  // FPLC=1bCK9tYwlJ1%2BO6HrupcXmXgL2ecsXAnOUefrRy3pXai2D21u9kBX1TE%2BNEe6enGKEAHZaCeED3sVvwtAn7GhBgDvpqSoKc%2FNt5voAgnHxc0xzVBMSHCNXCSF3otJeA%3D%3D; intercom-id-hu6d8oic=f4f05dce-eb53-4f97-af73-22e5d7fd6990; intercom-device-id-hu6d8oic=d56d5a41-b608-485c-8671-e87bf863ca4d; __cuid=420c9fe28c0745dbb861d2f17dedc8dd; amp_fef1e8=df86fef1-f666-4af6-ad0e-88c7b5bbc1f3R...1hl0augvp.1hl0aukna.3.0.3; _ga_GTL0Z41XQB=GS1.1.1706184329.1.1.1706188132.0.0.0; intercom-session-hu6d8oic=cXdLMHBwZVFBdjZzcGJRbHphb0tKM29WcTEwWnJGYmJtWkwxZ2txZ1NrdURNVTV6OVlwTHd4SzUxVTVMOXhwVS0tekcvVk44WU5qazR5ZjM1cU9wdDZVZz09--f4410d613d416b5aff89496ea8404027b4906ba7' \
  //            -H 'dnt: 1' \
  //            -H 'referer: https://app.abby.fr/billing/documents/invoices' \
  //            -H 'sec-ch-ua: "Not_A Brand";v="8", "Chromium";v="120", "Brave";v="120"' \
  //            -H 'sec-ch-ua-mobile: ?0' \
  //            -H 'sec-ch-ua-platform: "macOS"' \
  //            -H 'sec-fetch-dest: empty' \
  //            -H 'sec-fetch-mode: cors' \
  //            -H 'sec-fetch-site: same-origin' \
  //            -H 'sec-gpc: 1' \
  //            -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36
  // (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  //            -H 'x-amazon-referer: app-abby.com' \
  //            --compressed

}
