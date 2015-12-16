package com.pdl.server.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.ivoa.pdr.commons.JobBean;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GWTPDLServer implements EntryPoint {

	private int idUser;
	private String mail;
	private String gridId;

	private String urlMainServlet;

	final Label jobIdLabel = new Label();
	final Label jobPhaseLabel = new Label();
	final Label userLabel = new Label();
	final Label demandDateLabel = new Label();
	final Label finishingDateLabel = new Label();

	private JobBean[] jobs;
	private JobBean detailedJob;

	private VerticalPanel panel1;
	private VerticalPanel panel2;
	private HorizontalPanel panel0;

	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final JobServiceAsync jobService = GWT.create(JobService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		mail = com.google.gwt.user.client.Window.Location.getParameter("mail");
		idUser = Integer.parseInt(com.google.gwt.user.client.Window.Location
				.getParameter("userId"));

		gridId = com.google.gwt.user.client.Window.Location
				.getParameter("gridId");
		if (null == gridId || gridId.equalsIgnoreCase("")) {
			gridId = "None";
		}

		Label userIdentity = new Label();

		if (gridId.equalsIgnoreCase("None")) {
			userIdentity.setText("Job list for user " + mail);
		} else {
			userIdentity.setText("Job list for user " + mail + " and GridId = "
					+ gridId);
		}

		userIdentity.getElement().getStyle().setFontSize(1.5, Unit.EM);
		userIdentity.setPixelSize(1000, 40);

		panel1 = new VerticalPanel();

		panel2 = new VerticalPanel();
		panel0 = new HorizontalPanel();

		RootPanel.get("pdlContainer").add(userIdentity);

		RootPanel.get("pdlContainer").add(panel0);

		panel0.add(panel1);
		panel0.add(panel2);

		jobService.getJobList(idUser, mail, gridId, new JobsCallBack());
		jobService.getURLmainServlet(new urlCallBack());

	}

	private void updateDetailedPanel() {

		panel2.clear();

		VerticalPanel tempPanel = new VerticalPanel();
		VerticalPanel vPanelResults = new VerticalPanel();

		// if the detailed job has results
		if (detailedJob.getJobResults().size() > 0) {

			for (Entry<String, String> result : detailedJob.getJobResults()
					.entrySet()) {

				HorizontalPanel hPanelResults = new HorizontalPanel();

				String resultUrl = result.getValue();
				String resultName = result.getKey();

				Label resultNameLabel = new Label();
				resultNameLabel.setText(resultName + ": ");

				Anchor resultUrlLink = new Anchor(resultUrl, resultUrl);

				hPanelResults.add(resultNameLabel);
				hPanelResults.add(resultUrlLink);

				vPanelResults.add(hPanelResults);
			}
		}

		boolean running = detailedJob.getPhase().equalsIgnoreCase("running");
		boolean pending = detailedJob.getPhase().equalsIgnoreCase("pending");
		String buttonText;
		if (running || pending) {
			buttonText = "Stop this job";
		} else {
			buttonText = "Delete this job";
		}

		Button stopJobButton = new Button();
		stopJobButton.setText(buttonText);
		stopJobButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (Window.confirm("Are you sure for deleting this job?")) {
					panel2.clear();
					panel1.clear();

					// deleting the selected job
					jobService.deleteJob(idUser, mail, detailedJob.getIdJob(),
							new JobDeletionCallBack());
				}

			}
		});

		CaptionPanel captionpanel = new CaptionPanel();
		captionpanel.setCaptionText("Detail for the selected Job (Id="
				+ detailedJob.getIdJob() + ")");

		CellTable<Couple> detailedTable = new CellTable<Couple>();
		detailedTable
				.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		SimplePager detailedpager = new SimplePager(TextLocation.CENTER,
				pagerResources, true, 10, true);
		detailedpager.setDisplay(detailedTable);

		// Create a data provider.
		ListDataProvider<Couple> dataProvider = new ListDataProvider<Couple>();

		// preparing the list of Data
		List<Couple> paramsList = new ArrayList<GWTPDLServer.Couple>();
		for (Map.Entry<String, String> entry : detailedJob
				.getJobConfiguration().entrySet()) {
			paramsList.add(new Couple(entry.getKey(), entry.getValue()));
		}

		// Add the data to the data provider, which automatically pushes it to
		// the
		// widget.
		dataProvider.setList(paramsList);

		// Add a text column to show the id.
		TextColumn<Couple> keyColumn = new TextColumn<Couple>() {
			@Override
			public String getValue(Couple object) {
				return object.key;
			}
		};
		keyColumn.setSortable(true);
		detailedTable.addColumn(keyColumn, "Parameter Name");

		// Add a text column to show the Phase.
		TextColumn<Couple> valueColumn = new TextColumn<Couple>() {
			@Override
			public String getValue(Couple object) {
				return object.value;
			}
		};
		valueColumn.setSortable(true);
		detailedTable.addColumn(valueColumn, "Parameter Value");

		// test sorting
		ListHandler<Couple> columnSortHandler = new ListHandler<GWTPDLServer.Couple>(
				dataProvider.getList());
		columnSortHandler.setComparator(keyColumn,
				new Comparator<GWTPDLServer.Couple>() {
					@Override
					public int compare(Couple o1, Couple o2) {
						// Compare the name columns.
						if (o1 != null) {
							return (o2 != null) ? o2.key.compareTo(o1.key) : 1;
						}
						return -1;
					}
				});

		// end test sorting

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(detailedTable);

		// We know that the data is sorted alphabetically by default.
		detailedTable.getColumnSortList().clear();
		detailedTable.getColumnSortList().push(keyColumn);

		detailedTable.addColumnSortHandler(columnSortHandler);

		ColumnSortEvent.fire(detailedTable, detailedTable.getColumnSortList());

		// Set the total row count. This isn't strictly necessary, but it
		// affects
		// paging calculations, so its good habit to keep the row count up to
		// date.
		detailedTable.setRowCount(paramsList.size(), true);

		detailedTable.setPageSize(paramsList.size());

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element

		tempPanel.add(stopJobButton);
		tempPanel.add(vPanelResults);
		tempPanel.add(detailedTable);
		tempPanel.add(detailedpager);
		captionpanel.add(tempPanel);
		panel2.add(captionpanel);

	}

	private void updatePanel() {

		panel1.clear();

		// Create a CellTable.
		CellTable<JobBean> table = new CellTable<JobBean>();
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER,
				pagerResources, true, 10, true);
		pager.setDisplay(table);

		// Create a data provider.
		ListDataProvider<JobBean> dataProvider = new ListDataProvider<JobBean>();

		// Preparing the list to include
		List<JobBean> jobsList = new ArrayList<JobBean>();
		for (int i = 0; i < jobs.length; i++) {
			jobsList.add(jobs[i]);
		}

		// Add the data to the data provider, which automatically pushes it to
		// the
		// widget.
		dataProvider.setList(jobsList);

		// Add a selection model to handle user selection.
		final SingleSelectionModel<JobBean> selectionModel = new SingleSelectionModel<JobBean>();
		table.setSelectionModel(selectionModel);
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					public void onSelectionChange(SelectionChangeEvent event) {
						JobBean selected = selectionModel.getSelectedObject();
						if (selected != null) {
							// Get the selected ID
							int selectedJobID = selected.getIdJob();

							// Get the details of the selected job, from its ID
							jobService.getDetailedJob(idUser, mail,
									selectedJobID, new JobCallBacks());
						}
					}
				});

		// Add a text column to show the id.
		TextColumn<JobBean> idColumn = new TextColumn<JobBean>() {
			@Override
			public String getValue(JobBean object) {
				return object.getIdJob().toString();
			}
		};
		// idColumn.setSortable(true);
		table.addColumn(idColumn, "Job Id");

		// Add a text column to show the Phase.
		TextColumn<JobBean> phaseColumn = new TextColumn<JobBean>() {
			@Override
			public String getValue(JobBean object) {
				return object.getPhase();
			}
		};
		// phaseColumn.setSortable(true);
		table.addColumn(phaseColumn, "Job Phase");

		// Add a text column to show the demand Date.
		TextColumn<JobBean> demandDateColumn = new TextColumn<JobBean>() {
			@Override
			public String getValue(JobBean object) {
				return object.getDemandDate();
			}
		};
		// demandDateColumn.setSortable(true);
		table.addColumn(demandDateColumn, "Demand Date");

		// Add a text column to show the finishing Date.
		TextColumn<JobBean> finishingColumn = new TextColumn<JobBean>() {
			@Override
			public String getValue(JobBean object) {
				return object.getFinishingDate();
			}
		};
		// finishingColumn.setSortable(true);
		table.addColumn(finishingColumn, "End Date");

		// Connect the table to the data provider.
		dataProvider.addDataDisplay(table);

		// Set the total row count. This isn't strictly necessary, but it
		// affects
		// paging calculations, so its good habit to keep the row count up to
		// date.
		table.setRowCount(jobsList.size(), true);

		table.setPageSize(15);
		// table.setPageStart(1);

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		panel1.add(table);
		panel1.add(pager);

		Button downloadButton = new Button();
		downloadButton.setText("Download all the results");
		downloadButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				String linkURL = urlMainServlet + "getResultsByGrid?userId="
						+ idUser + "&mail=" + mail + "&gridId=" + gridId;
				Window.open(linkURL, "_new", "");
			}
		});

		panel1.add(downloadButton);

		panel1.setCellHorizontalAlignment(downloadButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		panel1.setCellHorizontalAlignment(pager,
				HasHorizontalAlignment.ALIGN_CENTER);

	}

	private void updateJob(JobBean[] jobs) {
		this.jobs = jobs;
	}

	private void updateDetailedJob(JobBean result) {
		this.detailedJob = result;

	}

	private void updateMainUrl(String url) {
		this.urlMainServlet = url;
	}

	private class JobCallBacks implements AsyncCallback<JobBean> {

		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			System.out.println("something went wrong");
		}

		@Override
		public void onSuccess(JobBean result) {
			updateDetailedJob(result);
			updateDetailedPanel();
		}

	}

	private class JobDeletionCallBack implements AsyncCallback<Boolean> {

		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			System.out.println("something went wrong in deleting job");
		}

		@Override
		public void onSuccess(Boolean result) {
			// After the job deletion, we build the list of the new jobs
			jobService.getJobList(idUser, mail, gridId, new JobsCallBack());
		}

	}

	private class urlCallBack implements AsyncCallback<String> {
		@Override
		public void onFailure(Throwable caught) {
		}

		@Override
		public void onSuccess(String result) {
			updateMainUrl(result);
		}

	}

	private class JobsCallBack implements AsyncCallback<JobBean[]> {

		@Override
		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub
			System.out.println("something went wrong");
		}

		@Override
		public void onSuccess(JobBean[] results) {
			updateJob(results);
			updatePanel();
		}

	}

	private class Couple {
		public Couple(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		private String key;
		private String value;
	}

}
