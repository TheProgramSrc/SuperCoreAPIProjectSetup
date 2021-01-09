package xyz.theprogramsrc.supercoreapiprojectsetup.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class HttpRequest {

    public static HttpRequest connect(String url) throws IOException{
        return new HttpRequest(((HttpURLConnection) new URL(url).openConnection()));
    }

    private final HttpURLConnection httpURLConnection;

    public HttpRequest(HttpURLConnection httpURLConnection) throws IOException{
        this.httpURLConnection = httpURLConnection;
        this.httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36");
        this.httpURLConnection.connect();
    }

    public boolean errorOnConnect() throws IOException{
        return !(resCode() + "").startsWith("2");
    }

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    public int resCode() throws IOException {
        return this.getHttpURLConnection().getResponseCode();
    }

    public String resMessage() throws IOException {
        return this.getHttpURLConnection().getResponseMessage();
    }

    public List<String> response() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getHttpURLConnection().getInputStream())).lines().collect(Collectors.toList());
    }

    public String stringResponse() throws IOException {
        return String.join("", response());
    }
}
