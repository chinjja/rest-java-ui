package com.chinjja.rest;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

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
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class App extends JFrame {
	final OkHttpClient client = new OkHttpClient();
	final JsonParser parser = new JsonParser();
	private JPanel contentPane;
	private final JPanel panel = new JPanel();
	private final JButton btnNewButton = new JButton("Create");
	private final EmployeeTable table = new EmployeeTable(this);
	private final JSpinner pageSize = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));

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
		
		contentPane.add(table, BorderLayout.CENTER);
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
	
	public Single<JsonObject> loadFromServer() {
		HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(8080).addPathSegment("api").addPathSegment("employees").addQueryParameter("size", ""+getPageSize()).build();
		return loadFromServer(url);
	}
	
	public Single<JsonObject> loadFromServer(String url) {
		return loadFromServer(HttpUrl.parse(url));
	}
	
	public Single<JsonObject> loadFromServer(HttpUrl url) {
		Single<JsonObject> get = Single.create(s -> {
			Call call = client.newCall(new Request.Builder().get().url(url).build());
			call.enqueue(new Callback() {
				@Override
				public void onResponse(Response response) throws IOException {
					try(ResponseBody body = response.body()) {
						s.onSuccess(parser.parse(body.string()).getAsJsonObject());
					}
				}
				
				@Override
				public void onFailure(Request request, IOException e) {
					s.onError(e);
				}
			});
		});
		
		return Single
		.just(get)
		.observeOn(Schedulers.io())
		.flatMap(x -> x)
		.observeOn(SwingSchedulers.edt())
		.doOnSuccess(x -> table.update(x));
	}
	
	public Single<JsonObject> create(JsonObject employee) {
		Single<JsonElement> post = Single.create(s -> {
			RequestBody body = RequestBody.create(MediaType.parse("application/json"), employee.toString());
			HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(8080).addPathSegment("api").addPathSegment("employees").build();
			Call call = client.newCall(new Request.Builder().post(body).url(url).build());
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
		
		return Single
		.just(post)
		.observeOn(Schedulers.io())
		.flatMap(x -> x)
		.flatMap(x -> loadFromServer())
		.flatMap(x -> {
			String url = x.get("_links").getAsJsonObject().get("last").getAsJsonObject().get("href").getAsString();
			return loadFromServer(url);
		});
	}

	public void delete(long id) {
		
	}
}
