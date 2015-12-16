package com.pdl.server.client;

import net.ivoa.pdr.commons.JobBean;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface JobServiceAsync {
	void getDetailedJob(Integer idUser, String mail, Integer idJob, AsyncCallback<JobBean> callback)
			throws IllegalArgumentException;
	
	void getJobList(Integer idUser, String mail, String gridId,  AsyncCallback<JobBean[]> callback);
	
	void deleteJob(Integer idUser, String mail, Integer idJob, AsyncCallback<Boolean> callback);
	
	void getURLmainServlet(AsyncCallback<String> callback);
}
