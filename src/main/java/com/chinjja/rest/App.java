package com.chinjja.rest;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.ejlchina.okhttps.OkHttps;
import com.ejlchina.stomp.Stomp;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hu.akarnokd.rxjava3.swing.SwingSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class App extends JFrame {
	final CookieJar cookieJar = new CookieJar() {
		private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

		@Override
		public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
			cookieStore.put(url.host(), cookies);
		}
		
		@Override
		public List<Cookie> loadForRequest(HttpUrl url) {
			List<Cookie> cookies = cookieStore.get(url.host());
			return cookies != null ? cookies : Collections.emptyList();
		}
	};
	final HttpUrl baseUrl = new HttpUrl.Builder().scheme("http").host("localhost").port(8080).addPathSegment("api").addPathSegment("employees").build();
	private OkHttpClient _client;
	public synchronized OkHttpClient client() {
		if(_client == null) {
			_client = new OkHttpClient.Builder().cookieJar(cookieJar).build();
		}
		return _client;
	}
	JsonObject properties;
	JsonObject links;
	static final JsonParser parser = new JsonParser();
	static final Gson gson = new Gson();
	private final EmployeeModel model = new EmployeeModel();
	private JPanel contentPane;
	private final JPanel panel = new JPanel();
	private final JButton create = new JButton("Create");
	private final JPanel panel_1 = new JPanel();
	private final JSpinner pageSize = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
	private final JButton delete = new JButton("Delete");
	private final JPanel buttons = new JPanel();
	private final JButton first = new JButton("<<");
	private final JButton prev = new JButton("<");
	private final JButton next = new JButton(">");
	private final JButton last = new JButton(">>");
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTable table = new JTable(model);
	private final JButton update = new JButton("Update");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new NimbusLookAndFeel());
					App frame = new App();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public App() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				Single
				.create(s -> s.onSuccess(client()))
				.subscribeOn(Schedulers.io())
				.subscribe();
				
				showSignIn();
			}
		});
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		contentPane.add(panel, BorderLayout.NORTH);
		create.setEnabled(false);
		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateDialog dlg = new CreateDialog(App.this, null);
				dlg.pack();
				dlg.setLocationRelativeTo(getRootPane());
				dlg.setVisible(true);
			}
		});
		
		panel.add(create);
		delete.setEnabled(false);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if(row == -1) return;
				
				delete(model.get(row)).subscribe();
			}
		});
		update.setEnabled(false);
		update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if(row == -1) return;
				
				CreateDialog dlg = new CreateDialog(App.this, model.get(row));
				dlg.pack();
				dlg.setLocationRelativeTo(getRootPane());
				dlg.setVisible(true);
			}
		});
		
		panel.add(update);
		
		panel.add(delete);
		
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_1.add(buttons, BorderLayout.SOUTH);
		buttons.setLayout(new GridLayout(0, 4, 0, 0));
		first.setEnabled(false);
		
		first.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(first);
		prev.setEnabled(false);
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(prev);
		next.setEnabled(false);
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(next);
		last.setEnabled(false);
		last.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(last);
		
		panel_1.add(scrollPane, BorderLayout.CENTER);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowVerticalLines(true);
		table.setShowHorizontalLines(true);
		
		scrollPane.setViewportView(table);
		pageSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				loadFromServer().subscribe();
			}
		});
		
		contentPane.add(pageSize, BorderLayout.SOUTH);
	}
	
	public int getPageSize() {
		return (Integer)pageSize.getValue();
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize.setValue(pageSize);
	}
	
	private void showSignIn() {
		SignInDialog dlg = new SignInDialog(this);
		dlg.pack();
		dlg.setLocationRelativeTo(getRootPane());
		dlg.setVisible(true);
	}
	
	public boolean signIn(String username, char[] password) {
		String key = "Authorization";
		String value = "Basic " + Base64.getEncoder().encodeToString((username+":"+new String(password)).getBytes());
		Request req = new Request.Builder().get().url("http://localhost:8080/api").addHeader(key, value).build();
		try {
			Response res = client().newCall(req).execute();
			boolean success = res.isSuccessful();
			if(success) {
				loadFromServer().subscribe();
				
				Stomp stomp = Stomp.over(OkHttps.webSocket("ws://localhost:8080/payroll").addHeader(key, value));
				stomp.connect();
				
				stomp
				.topic("/newEmployee", msg -> {
					_loadFromServer()
					.flatMap(x -> {
						if(links.has("last")) {
							return navigate(href(links, "last"));
						} else {
							return _response(x);
						}
					}).subscribe();
				})
				.topic("/updateEmployee", msg -> {
					navigate(href(links, "self")).subscribe();
				})
				.topic("/deleteEmployee", msg -> {
					navigate(href(links, "self")).subscribe();
				});
				
				SwingUtilities.invokeLater(() -> {
					create.setEnabled(true);
					update.setEnabled(true);
					delete.setEnabled(true);
				});
			}
			res.close();
			return success;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Single<List<ResponseJson>> loadFromServer() {
		return Single
				.just(0)
				.observeOn(Schedulers.io())
				.flatMap(x -> _loadFromServer())
				.flatMap(res -> _response(res));
	}
	
	private Single<ResponseJson> _loadFromServer() {
		return Single
				.just(baseUrl.newBuilder().addQueryParameter("size", ""+getPageSize()).build())
				.flatMap(url -> bridge(client().newCall(new Request.Builder().get().url(url).build())))
				.flatMap(resp -> Single
					.just(href(resp.entity, "_links", "profile"))
					.map(url -> new Request.Builder().get().url(url).header("accept", "application/schema+json").build())
					.flatMap(req -> bridge(client().newCall(req)))
					.doOnSuccess(schema -> {
						JsonObject root = object(schema.entity, "properties");
						properties = root.deepCopy();
						for(String key : root.keySet()) {
							JsonElement property = root.get(key);
							if(has(property, "$ref")) {
								properties.remove(key);
							}
							else if(has(property, "format") && "uri".equals(string(property, "format"))) {
								properties.remove(key);
							}
						}
					})
					.map(schema -> resp))
				.doOnSuccess(resp -> links = object(resp.entity, "_links"));
	}
	
	public Single<List<ResponseJson>> navigate(String url) {
		return Single
				.just(url)
				.observeOn(Schedulers.io())
				.map(x -> new Request.Builder().get().url(x).build())
				.flatMap(req -> bridge(client().newCall(req)))
				.doOnSuccess(res -> links = object(res.entity, "_links"))
				.flatMap(res -> _response(res));
	}
	
	private Single<List<ResponseJson>> _response(ResponseJson response) {
		return Single
				.just(response)
				.flatMap(resp -> Observable
					.fromIterable(array(resp.entity, "_embedded", "employees"))
					.map(x -> href(x, "_links", "self"))
					.map(url -> new Request.Builder().get().url(url).build())
					.concatMapEager(req -> bridge(client().newCall(req)).toObservable())
					.toList()
				)
				.observeOn(SwingSchedulers.edt())
				.doOnSuccess(x -> {
					update(x);
				});
	}
	
	public Single<ResponseJson> create(JsonObject data) {
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), data.toString());
		return Single
				.just(baseUrl)
				.observeOn(Schedulers.io())
				.map(url -> new Request.Builder().post(body).url(url).build())
				.flatMap(req -> bridge(client().newCall(req)))
				;
	}
	
	public Single<ResponseJson> update(ResponseJson employee, JsonObject data) {
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), data.toString());
		return Single
				.just(href(employee.entity, "_links", "self"))
				.observeOn(Schedulers.io())
				.flatMap(url -> bridge(client().newCall(new Request.Builder().put(body).url(url).addHeader("if-match", employee.headers.get("etag")).build())))
				;
	}

	public Single<ResponseJson> delete(ResponseJson employee) {
		return Single
				.just(href(employee.entity, "_links", "self"))
				.observeOn(Schedulers.io())
				.flatMap(url -> bridge(client().newCall(new Request.Builder().delete().url(url).build())))
				;
	}
	
	static Single<ResponseJson> bridge(Call call) {
		return Single.create(s -> {
			call.enqueue(new Callback() {
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					JsonElement entity = parser.parse(response.body().string());
					ResponseJson res = new ResponseJson(response.code(), response.headers(), entity);
					if(response.isSuccessful()) {
						s.onSuccess(res);
					} else {
						s.onError(new ResponseJsonException(res));
					}
					response.close();
				}

				@Override
				public void onFailure(Call call, IOException e) {
					s.onError(e);
				}
			});
		});
	}
	
	public void update(Iterable<ResponseJson> obj) {
		model.clear();
		for(ResponseJson item : obj) {
			model.add(item);
		}
		
		HashMap<String, JButton> map = new HashMap<>();
		map.put("first", first);
		map.put("prev", prev);
		map.put("next", next);
		map.put("last", last);
		for(Map.Entry<String, JButton> e : map.entrySet()) {
			String key = e.getKey();
			JButton btn = e.getValue();
			if(links.has(key)) {
				btn.setEnabled(true);
				btn.setActionCommand(href(links.get(key)));
			} else {
				btn.setEnabled(false);
			}
		}
	}
	
	public static String string(JsonElement json, String...paths) {
		for(String path : paths) {
			if(!json.isJsonObject()) return null;
			json = json.getAsJsonObject().get(path);
		}
		if(json == null) return null;
		return json.getAsString();
	}
	
	public static Boolean bool(JsonElement json, String...paths) {
		for(String path : paths) {
			if(!json.isJsonObject()) return null;
			json = json.getAsJsonObject().get(path);
		}
		if(json == null) return null;
		return json.getAsBoolean();
	}
	
	public static String href(JsonElement json, String...paths) {
		json = element(json, paths);
		if(json == null) return null;
		return json.getAsJsonObject().get("href").getAsString();
	}
	
	public static boolean has(JsonElement json, String...paths) {
		json = element(json, paths);
		return json != null;
	}
	
	public static JsonElement element(JsonElement json, String...paths) {
		for(String path : paths) {
			if(!json.isJsonObject()) return null;
			json = json.getAsJsonObject().get(path);
		}
		return json;
	}
	
	public static JsonObject object(JsonElement json, String...paths) {
		for(String path : paths) {
			if(!json.isJsonObject()) return null;
			json = json.getAsJsonObject().get(path);
		}
		return (JsonObject)json;
	}
	
	public static JsonArray array(JsonElement json, String...paths) {
		for(String path : paths) {
			if(!json.isJsonObject()) return null;
			json = json.getAsJsonObject().get(path);
		}
		return (JsonArray)json;
	}
}
