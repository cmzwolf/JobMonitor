package com.pdl.server.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ivoa.pdr.business.JobBusiness;
import net.ivoa.pdr.business.UserBusiness;
import net.ivoa.pdr.commons.JobBean;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.pdl.server.client.JobService;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class JobServiceImpl extends RemoteServiceServlet implements JobService {

	public JobBean getDetailedJob(Integer idUser, String mail, Integer idJob)
			throws IllegalArgumentException {
		JobBean toReturn = new JobBean();
		try {
			Integer userIdInDB = UserBusiness.getInstance().getIdUserByMail(
					mail);
			String demandDateforUser = JobBusiness.getInstance()
					.getDateWhereUserAskedTheJob(idUser, idJob);

			String notificationDate = JobBusiness.getInstance()
					.getDateWhereUserReceiveNotificationForJob(idUser, idJob);

			if (null == demandDateforUser
					|| demandDateforUser.equalsIgnoreCase("")
					|| idUser != userIdInDB) {
				toReturn.setErrors("The user " + mail
						+ " never asked the job having the ID " + idJob);
			} else {
				toReturn = JobBusiness.getInstance().getJobBeanFromIdJob(idJob);
				String phase = JobBusiness.getInstance().computeJobPhase(
						toReturn);
				toReturn.setPhase(phase);
				toReturn.setDemandDate(demandDateforUser);
				toReturn.setFinishingDate(notificationDate);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	@Override
	public JobBean[] getJobList(Integer idUser, String mail, String gridId)
			throws IllegalArgumentException {

		List<JobBean> jobsList = new ArrayList<JobBean>();

		try {
			Integer userIdForUser = idUser;
			Integer userIdInDB = UserBusiness.getInstance().getIdUserByMail(
					mail);
			if (userIdForUser.equals(userIdInDB)) {

				List<Integer> jobsIds;

				if (gridId.equalsIgnoreCase("None")) {
					jobsIds = JobBusiness.getInstance()
							.getListOfJobsAskedByUser(idUser);
				} else {
					jobsIds = JobBusiness.getInstance()
							.getListOfJobsAskedByUserAndGridId(userIdInDB,
									gridId);
				}

				for (Integer currentJobId : jobsIds) {

					JobBean currentJob = JobBusiness.getInstance()
							.getJobBeanFromIdJobLight(currentJobId);
					String demandDate = JobBusiness.getInstance()
							.getDateWhereUserAskedTheJob(userIdInDB,
									currentJobId);
					String phase = JobBusiness.getInstance().computeJobPhase(
							currentJob);

					currentJob.setDemandDate(demandDate);
					currentJob.setPhase(phase);

					jobsList.add(currentJob);
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return convertListToTable(jobsList);
	}

	private JobBean[] convertListToTable(List<JobBean> jobList) {
		int tableSize = jobList.size();
		JobBean[] jobTable = new JobBean[tableSize];
		for (int i = 0; i < tableSize; i++) {
			jobTable[i] = jobList.get(i);
		}
		return jobTable;
	}

	private JobBean MockCreator(Integer IdJob, String mail) {
		JobBean temp = new JobBean();
		temp.setIdJob(IdJob);
		temp.setPhase("running");
		temp.setDemandDate("03/07/2014");
		temp.setFinishingDate("04/07/2014");

		Map<String, String> parameterMap = new HashMap<String, String>();
		for (int i = 0; i < 30; i++) {
			parameterMap.put("param" + i, i + "");
		}
		temp.setJobConfiguration(parameterMap);

		Map<String, String> resultMap = new HashMap<String, String>();

		for (int i = 0; i < 3; i++) {
			resultMap.put("result" + i, "http://www.corriere.it");
		}
		temp.setJobResults(resultMap);

		return temp;
	}

	@Override
	public Boolean deleteJob(Integer idUser, String mail, Integer idJob) {
		Boolean success = true;
		try {
			Integer userIdForUser = idUser;
			Integer userIdInDB = UserBusiness.getInstance().getIdUserByMail(
					mail);
			if (userIdForUser.equals(userIdInDB)) {
				// delete the link between user and IdJob in Notification List
				UserBusiness.getInstance().cutLinkUserJob(userIdInDB, idJob);
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = false;
		}
		return success;
	}

}
