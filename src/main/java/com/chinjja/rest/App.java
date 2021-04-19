package com.chinjja.rest;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import hu.akarnokd.rxjava3.swing.SwingSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class App extends JFrame {
	final HttpUrl baseUrl = new HttpUrl.Builder().scheme("http").host("localhost").port(8080).addPathSegment("api").addPathSegment("employees").build();
	final OkHttpClient client = new OkHttpClient();
	JsonObject properties;
	static final JsonParser parser = new JsonParser();
	static final Gson gson = new Gson();
	private final EmployeeModel model = new EmployeeModel();
	private JPanel contentPane;
	private final JPanel panel = new JPanel();
	private final JButton btnNewButton = new JButton("Create");
	private final JPanel panel_1 = new JPanel();
	private final JSpinner pageSize = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
	private final JButton btnNewButton_1 = new JButton("Delete");
	private final JPanel buttons = new JPanel();
	private final JButton first = new JButton("<<");
	private final JButton prev = new JButton("<");
	private final JButton next = new JButton(">");
	private final JButton last = new JButton(">>");
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTable table = new JTable(model);

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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		contentPane.add(panel, BorderLayout.NORTH);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreateDialog dlg = new CreateDialog(App.this);
				dlg.setModalityType(ModalityType.APPLICATION_MODAL);
				dlg.pack();
				dlg.setLocationRelativeTo(getRootPane());
				dlg.setVisible(true);
			}
		});
		
		panel.add(btnNewButton);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if(row == -1) return;
				
				delete(model.get(row)).subscribe();
			}
		});
		
		panel.add(btnNewButton_1);
		
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_1.add(buttons, BorderLayout.SOUTH);
		buttons.setLayout(new GridLayout(0, 4, 0, 0));
		
		first.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(first);
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(prev);
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigate(e.getActionCommand()).subscribe();
			}
		});
		
		buttons.add(next);
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
		
		loadFromServer().subscribe();
	}
	
	public int getPageSize() {
		return (Integer)pageSize.getValue();
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize.setValue(pageSize);
	}
	
	public Single<JsonElement> loadFromServer() {
		return Single
				.just(baseUrl.newBuilder().addQueryParameter("size", ""+getPageSize()).build())
				.observeOn(Schedulers.io())
				.flatMap(url -> bridge(client.newCall(new Request.Builder().get().url(url).build())))
				.flatMap(x -> {
					String profile = string(x, "_links", "profile", "href");
					return bridge(client.newCall(new Request.Builder().get().url(profile).header("accept", "application/schema+json").build()))
							.doOnSuccess(res -> properties = object(res, "properties"))
							.map(res -> x);
				})
				.observeOn(SwingSchedulers.edt())
				.doOnSuccess(x -> {
					update(x.getAsJsonObject());
				});
	}
	
	public Single<JsonElement> navigate(String url) {
		return Single
				.just(0)
				.observeOn(Schedulers.io())
				.flatMap(x -> bridge(client.newCall(new Request.Builder().get().url(url).build())))
				.observeOn(SwingSchedulers.edt())
				.doOnSuccess(x -> update(x.getAsJsonObject()));
	}
	
	public Single<JsonElement> create(JsonObject employee) {
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), employee.toString());
		
		return Single
				.just(baseUrl)
				.observeOn(Schedulers.io())
				.flatMap(url -> bridge(client.newCall(new Request.Builder().post(body).url(url).build())))
				.map(url -> baseUrl.newBuilder().addQueryParameter("size", ""+getPageSize()).build())
				.flatMap(url -> bridge(client.newCall(new Request.Builder().get().url(url).build())))
				.flatMap(x -> {
					JsonElement last = element(x, "_links", "last");
					if(last != null) {
						return navigate(string(last, "href"));
					}
					return Single.just(x);
				});
	}

	public Single<JsonElement> delete(JsonObject employee) {
		return Single
				.just(string(employee, "_links", "self", "href"))
				.observeOn(Schedulers.io())
				.flatMap(url -> bridge(client.newCall(new Request.Builder().delete().url(url).build())))
				.flatMap(x -> loadFromServer());
	}
	
	static Single<JsonElement> bridge(Call call) {
		return Single.create(s -> {
			call.enqueue(new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					try(ResponseBody body = response.body()) {
						s.onSuccess(parser.parse(body.string()));
					}
				}
				
				@Override
				public void onFailure(Request request, IOException e) {
					s.onError(e);
				}
			});
		});
	}
	
	public void update(JsonObject obj) {
		JsonElement embedded = obj.get("_embedded");
		JsonArray employees = array(embedded, "employees");
		model.clear();
		for(int i = 0; i < employees.size(); i++) {
			model.add(employees.get(i).getAsJsonObject());
		}
		
		JsonObject links = (JsonObject)obj.get("_links");
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
				btn.setActionCommand(string(links.get(key), "href"));
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
