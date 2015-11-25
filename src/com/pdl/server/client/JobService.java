package com.pdl.server.client;

import net.ivoa.pdr.commons.JobBean;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface JobService extends RemoteService {
	JobBean getDetailedJob(Integer idUser, String mail, Integer idJob) throws IllegalArgumentException;
	
	JobBean[] getJobList(Integer idUser, String mail, String gridId) throws IllegalArgumentException;
	
	Boolean deleteJob(Integer idUser, String mail, Integer idJob);
}
