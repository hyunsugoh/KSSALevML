package com.levware.admin.service;


public class BatchJobDataVO {

	/** job_exe_id  */
	private int job_exe_id;
	
	/** 버전  */
	private int version;
	
	/**  */
	private int job_instance_id; 
	
	/** 생성시간  */
	private String create_time;
	
	/** 시작시간  */
	private String start_time;
	
	/** 종료시작  */
	private String end_time;
	
	/** 상태  */
	private String status;
	
	/** 종료코드  */
	private String exit_code;
	
	/** 종료메시지  */
	private String exit_message;
	
	/** LAST_UPDATED  */
	private String last_updated;
	
	
	private String job_config_loc;

	
	
	
	public int getJob_exe_id() {
		return job_exe_id;
	}




	public void setJob_exe_id(int job_exe_id) {
		this.job_exe_id = job_exe_id;
	}




	public int getVersion() {
		return version;
	}




	public void setVersion(int version) {
		this.version = version;
	}




	public int getJob_instance_id() {
		return job_instance_id;
	}




	public void setJob_instance_id(int job_instance_id) {
		this.job_instance_id = job_instance_id;
	}




	public String getCreate_time() {
		return create_time;
	}




	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}




	public String getStart_time() {
		return start_time;
	}




	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}




	public String getEnd_time() {
		return end_time;
	}




	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}




	public String getStatus() {
		return status;
	}




	public void setStatus(String status) {
		this.status = status;
	}




	public String getExit_code() {
		return exit_code;
	}




	public void setExit_code(String exit_code) {
		this.exit_code = exit_code;
	}




	public String getExit_message() {
		return exit_message;
	}




	public void setExit_message(String exit_message) {
		this.exit_message = exit_message;
	}




	public String getLast_updated() {
		return last_updated;
	}




	public void setLast_updated(String last_updated) {
		this.last_updated = last_updated;
	}




	public String getJob_config_loc() {
		return job_config_loc;
	}




	public void setJob_config_loc(String job_config_loc) {
		this.job_config_loc = job_config_loc;
	}




	@Override
	public String toString() {
		return "BatchJobDataVO [job_exe_id=" + job_exe_id + ", version=" + version + ", job_instance_id="
				+ job_instance_id + ", create_time=" + create_time + ", start_time=" + start_time + ", end_time="
				+ end_time + ", status=" + status + ", exit_code=" + exit_code + ", exit_message=" + exit_message
				+ ", last_updated=" + last_updated + ", job_config_loc=" + job_config_loc + "]";
	}
	
		
	

	
	

	
	
	
	
	
	
	
	
	
}
