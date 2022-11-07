package com.levware.admin.service;


public class BatchDataVO {

	/** STEP_EXECUTION_ID  */
	private int step_exe_id;
	
	/** 버전  */
	private int version;
	
	/** 단계이름  */
	private String step_name;
	
	/** */
	private String job_exe_id;
	
	/** 시작시간  */
	private String start_time;
	
	/** 종료시간  */
	private String end_time;
	
	/** 상태  */
	private String status;
	
	/** 커밋횟수  */
	private int commit_count;
	
	/** 읽은횟수  */
	private int read_count;
	
	/** FILTER_COUNT  */
	private int filter_count;
	
	/** WRITE_COUNT  */
	private int write_count;
	
	/** READ_SKIP_COUNT  */
	private int read_skip_count;
	
	/** WRITE_SKIP_COUNT  */
	private int write_skip_count;
	
	/** PROCESS_SKIP_COUNT  */
	private int process_skip_count;
	
	/** ROLLBACK_COUNT  */
	private int rollback_count;
	
	/** EXIT_CODE  */
	private String exit_code;
	
	/** EXIT_MESSAGE  */
	private String exit_message;
	
	/** LAST_UPDATED  */
	private String last_updated;
	
	/**M_DATA_CNV1 total count */
	private int cnv1_count;

	public int getStep_exe_id() {
		return step_exe_id;
	}

	public void setStep_exe_id(int step_exe_id) {
		this.step_exe_id = step_exe_id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getStep_name() {
		return step_name;
	}

	public void setStep_name(String step_name) {
		this.step_name = step_name;
	}

	public String getJob_exe_id() {
		return job_exe_id;
	}

	public void setJob_exe_id(String job_exe_id) {
		this.job_exe_id = job_exe_id;
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

	public int getCommit_count() {
		return commit_count;
	}

	public void setCommit_count(int commit_count) {
		this.commit_count = commit_count;
	}

	public int getRead_count() {
		return read_count;
	}

	public void setRead_count(int read_count) {
		this.read_count = read_count;
	}

	public int getFilter_count() {
		return filter_count;
	}

	public void setFilter_count(int filter_count) {
		this.filter_count = filter_count;
	}

	public int getWrite_count() {
		return write_count;
	}

	public void setWrite_count(int write_count) {
		this.write_count = write_count;
	}

	public int getRead_skip_count() {
		return read_skip_count;
	}

	public void setRead_skip_count(int read_skip_count) {
		this.read_skip_count = read_skip_count;
	}

	public int getWrite_skip_count() {
		return write_skip_count;
	}

	public void setWrite_skip_count(int write_skip_count) {
		this.write_skip_count = write_skip_count;
	}

	public int getProcess_skip_count() {
		return process_skip_count;
	}

	public void setProcess_skip_count(int process_skip_count) {
		this.process_skip_count = process_skip_count;
	}

	public int getRollback_count() {
		return rollback_count;
	}

	public void setRollback_count(int rollback_count) {
		this.rollback_count = rollback_count;
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

	public int getCnv1_count() {
		return cnv1_count;
	}

	public void setCnv1_count(int cnv1_count) {
		this.cnv1_count = cnv1_count;
	}

	@Override
	public String toString() {
		return "BatchDataVO [step_exe_id=" + step_exe_id + ", version=" + version + ", step_name=" + step_name
				+ ", job_exe_id=" + job_exe_id + ", start_time=" + start_time + ", end_time=" + end_time + ", status="
				+ status + ", commit_count=" + commit_count + ", read_count=" + read_count + ", filter_count="
				+ filter_count + ", write_count=" + write_count + ", read_skip_count=" + read_skip_count
				+ ", write_skip_count=" + write_skip_count + ", process_skip_count=" + process_skip_count
				+ ", rollback_count=" + rollback_count + ", exit_code=" + exit_code + ", exit_message=" + exit_message
				+ ", last_updated=" + last_updated + ", cnv1_count=" + cnv1_count + "]";
	}

	
	
	
	
	
	
	
	
	
}
