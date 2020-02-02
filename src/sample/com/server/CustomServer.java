package sample.com.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

// TODO CustomServerは組み込みたいクラス名に変える。
public class CustomServer implements HttpHandler {

	private static final int PORT = 50000;
	private static final String CONTEXT_PATH = "/Custom";
	private static final int CONNECTION_WAITING_COUNT = 2;

	public static void main(String[] args) throws IOException {
		System.out.println("プロセスID:" + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

		// TODO HTTPS化
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), CONNECTION_WAITING_COUNT);
		server.createContext(CONTEXT_PATH, new CustomServer());
		server.start();
		System.out.println("サーバが稼働しました。");
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		// Basic認証
		BasicAuthenticator authenticator = new BasicAuthenticator("my realm") {
			@Override
			public boolean checkCredentials(String arg0, String arg1) {
				// TODO Basic認証のチェック処理
				System.out.println("arg0:" + arg0);
				System.out.println("arg1:" + arg1);
				return false;
			}
		};

		authenticator.authenticate(exchange);

		// リクエスト取得 =============================================================
		System.out.println("■RequestURI:" + exchange.getRequestURI());
		System.out.println("■RequestQuery:" + getRequestParameter(exchange.getRequestURI()));
		System.out.println("■RequestMethod:" + exchange.getRequestMethod());
		System.out.println("■RequestHeader:");
		Headers resHeaders = exchange.getRequestHeaders();
		for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		System.out.println("■RequestBody:");
		try (InputStream in = exchange.getRequestBody()) {
			int data;
			StringBuilder sb = new StringBuilder();
			while ((data = in.read()) != -1) {
				sb.append((char) data);
			}
			System.out.println(sb);
		}

		// レスポンス設定 ==============================================================
		// Header設定
		Headers headers = exchange.getResponseHeaders();
		headers.set("Content-type", "text/plain");

		// 2番目の引数=0  → bodyを返すとき
		// 2番目の引数=-1 → bodyを返さないとき
		//		exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, -1);
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
		//		exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);

		try (OutputStream out = exchange.getResponseBody()) {
			StringBuilder sb = new StringBuilder();
			sb.append("line 1").append(System.lineSeparator());
			sb.append("line 2").append(System.lineSeparator());
			sb.append("line 3").append(System.lineSeparator());

			out.write(sb.toString().getBytes());
		}
	}

	private Map<String, String> getRequestParameter(URI uri) {
		Map<String, String> result = new HashMap<String, String>();
		String query = uri.getQuery();
		if (query == null || query.isEmpty()) {
			return result;
		}

		for (String queryUnit : query.split("&")) {
			// key名に=が入ると崩れる
			String[] keyValue = queryUnit.split("=", 2);
			result.put(keyValue[0], keyValue[1]);
		}

		return result;
	}

}
