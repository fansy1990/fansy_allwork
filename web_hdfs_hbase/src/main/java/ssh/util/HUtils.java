package ssh.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import common.utils.HadoopUtil;
import common.utils.JarUtil;
import common.utils.PropertiesUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.ShortWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.JobStatus;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ssh.eum.MRInfo;
import ssh.eum.MRLock;

public class HUtils {
	private static Configuration conf;

	private static Map<String, String> confMap = new HashMap<>();
	private static Job currJob;
	private static FileSystem fs;
	public static final String hbase_properties = "/hbase.properties";
	public static final String ssh_properties = "/ssh.properties";

	private static String hadoopPassword = "null";
	private static String hadoopUserName = "root";// 初始值，root字符串

	// private static Admin admin = null;
	private static Configuration hbaseConfiguration = null;
	private static Connection hbaseConnection = null;

	private static JobClient jobClient = null;

	public static final Logger log = LoggerFactory.getLogger(HUtils.class);

	private static Map<MRInfo, String> mrInfo = new HashMap<>();

	private static MRLock mrLock = MRLock.NOTLOCKED;

	/**
	 * 在修改的时候加上同步
	 * 
	 * @param mrError
	 */
	public static synchronized void addMrError(MRInfo key, String mrError) {
		HUtils.mrInfo.put(key, mrError);
	}

	/**
	 * 检查用户是否对hdfs 目录folder 有读权限 规则： 判断是否和档案拥有者同名，同名直接返回true； 不同名，直接归为其他用户
	 * 
	 * @param folder
	 * @param permission
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static boolean checkHdfsAuth(String folder, String permission)
			throws IllegalArgumentException, IOException {
		FileStatus fileStatus = getFs().getFileStatus(new Path(folder));
		if (fileStatus.getOwner().equals(getHadoopUserName()))
			return true;
		log.info("userAction:{},groupAction:{},otherAction:{}", new Object[] {
				fileStatus.getPermission().getUserAction().SYMBOL,
				fileStatus.getPermission().getGroupAction().SYMBOL,
				fileStatus.getPermission().getOtherAction().SYMBOL });
		if (fileStatus.getPermission().getOtherAction().SYMBOL
				.contains(permission))
			return true;
		return false;
	}

	/**
	 * 清空
	 */
	public static synchronized void clearMrInfo() {
		HUtils.mrInfo.clear();
	}

	public static Configuration getConf() {
		if (conf == null) {
			init();
			conf = HadoopUtil.getConf();// 获取基础配置
			conf.set("mapreduce.job.jar", JarUtil.jar(JarUtil.class));// 设置jar包路径

			// 设置HBase的配置
			conf.set("hbase.master", confMap.get("hbase.master"));
			conf.set("hbase.rootdir", confMap.get("hbase.rootdir"));
			conf.set("hbase.zookeeper.quorum",
					confMap.get("hbase.zookeeper.quorum"));
			conf.set("hbase.zookeeper.property.clientPort",
					confMap.get("hbase.zookeeper.property.clientPort"));
		}
		return conf;
	}

	public static Job getCurrJob() {
		return currJob;
	}

	public static FileSystem getFs() {

		return fs;
	}

	public static String getHadoopPassword() {
		return hadoopPassword;
	}

	public static String getHadoopUserName() {
		return hadoopUserName;
	}

	public static Connection getHBaseConnection() {
		if (hbaseConnection == null) {
			hbaseConfiguration = HBaseConfiguration.create();
			hbaseConfiguration.set("hbase.master", confMap.get("hbase.master"));
			hbaseConfiguration.set("hbase.rootdir",
					confMap.get("hbase.rootdir"));
			hbaseConfiguration.set("hbase.zookeeper.quorum",
					confMap.get("hbase.zookeeper.quorum"));
			hbaseConfiguration.set("hbase.zookeeper.property.clientPort",
					confMap.get("hbase.zookeeper.property.clientPort"));
			try {
				hbaseConnection = ConnectionFactory
						.createConnection(hbaseConfiguration);
			} catch (IOException e) {
				log.error("获取hbase 连接异常！");
				hbaseConnection = null;
			}
		}
		return hbaseConnection;
	}

	public static JobClient getJobClient() {
		if (jobClient == null) {
			try {
				jobClient = new JobClient(getConf());
			} catch (IOException e) {
				e.printStackTrace();
				log.info("获取JobClient异常!");
			}
		}
		return jobClient;
	}

	public static String getJobId() {
		long start = System.currentTimeMillis();
		while (noJobId()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info(" Getting job id ...");
		}
		long end = System.currentTimeMillis();

		log.info("获取jobId，耗时：" + (end - start) * 1.0 / 1000 + "s");
		return currJob.getJobID().toString();
	}

	private static JobID getJobIdFromStr(String jobId) {
		String[] jobStrings = jobId.split(Utils.UNDERLINE, -1);
		return new JobID(jobStrings[1], Integer.parseInt(jobStrings[2]));
	}

	public static Map<MRInfo, String> getMrInfo() {
		return mrInfo;
	}

	public static MRLock getMrLock() {
		return mrLock;
	}

	/**
	 * 根据JobID 获得进度
	 * 
	 * @param jobId
	 * @return
	 */
	public static String getMRProgress(String jobId) {
		RunningJob runningJob = null;
		JobStatus jobStatus = null;

		try {
			runningJob = getJobClient().getJob(getJobIdFromStr(jobId));
			// System.out.println(":==" + runningJob);
			if (runningJob == null)
				return "0.00";
			jobStatus = runningJob.getJobStatus();

			// 如果正在运行，需要判断任务是否包含reduce，否则采用不同的计算法方式
			double progress = 0.0;
			if (jobStatus.getState().equals(JobStatus.State.RUNNING)) {
				if (getCurrJob().getConfiguration().getInt(
						"mapreduce.job.reduces", 1) == 0) {// no
															// reducer,直接使用mapper进度
					progress = jobStatus.getMapProgress() * 100;
				} else {// 使用mapper和reducer的进度和的平均
					progress = (jobStatus.getMapProgress() + jobStatus
							.getReduceProgress()) / 2 * 100;
				}
				return Utils.twoPlaceDecimal.format(progress);
			}
			if (jobStatus.getState().equals(JobStatus.State.PREP))
				return "0.00";

			if (jobStatus.getState().equals(JobStatus.State.SUCCEEDED))
				return "success";
			if (jobStatus.getState().equals(JobStatus.State.FAILED))
				return "error";
			if (jobStatus.getState().equals(JobStatus.State.KILLED))
				return "kill";

		} catch (IOException e) {
			e.printStackTrace();
			log.info("获取任务：" + jobId + "状态异常!");
		}

		return "0.00";
	}

	/**
	 * 获取hadoop.properties中的值
	 * 
	 * @param property
	 * @return
	 */
	public static String getPropertyValue(String property) {
		if (confMap == null)
			return null;
		return confMap.get(property);
	}

	/**
	 * 获得writable 的实际值
	 * 
	 * @param writable
	 * @return
	 */
	private static String getValue(Writable writable) {
		if (writable instanceof Text) {
			return writable.toString();
		}
		if (writable instanceof LongWritable) {
			return String.valueOf(((LongWritable) writable).get());
		}
		if (writable instanceof IntWritable) {
			return String.valueOf(((IntWritable) writable).get());
		}
		if (writable instanceof ShortWritable) {
			return String.valueOf(((ShortWritable) writable).get());
		}

		if (writable instanceof DoubleWritable) {
			return String.valueOf(((DoubleWritable) writable).get());
		}
		if (writable instanceof BooleanWritable) {
			return String.valueOf(((BooleanWritable) writable).get());
		}
		return null;
	}

    /**
     * 初始化
     * 1. 设置 Hadoop User
     */
	private static void init() {
		System.setProperty("HADOOP_USER_NAME", getHadoopUserName());

        // 获取HBase 配置文件
        confMap.putAll(PropertiesUtil.getProperties(hbase_properties));

        // 获取ssh 配置文件
        confMap.putAll(PropertiesUtil.getProperties(ssh_properties));

	}

	/**
	 * 在更新 绑定的HDFS用户时，需要重新初始化FS
	 */
	public static void initFs() {
		try {
			conf = null;
			fs = FileSystem.get(getConf());
			log.info("Hadoop文件系统FS被初始化！");
		} catch (IOException e) {
			e.printStackTrace();
			log.info("获取Hadoop文件系统异常!");
			fs = null;
		}
	}

	/**
	 * MR任务缓存包括 jobId 和 mrInfo，Job
	 */
	public static void initMRCache() {
		clearMrInfo();
		HUtils.currJob = null;
	}

	private static boolean noJobId() {
		if (currJob == null || currJob.getJobID() == null)
			return true;

		return false;
	}

	/**
	 * 读取序列文件
	 * 
	 * @param fileName
	 * @param records
	 * @return
	 * @throws IOException
	 */
	public static String readSeq(String fileName, int records)
			throws IOException {
		Configuration conf = getConf();
		Reader reader = null;
		StringBuffer buffer = new StringBuffer();
		try {
			reader = new Reader(conf, Reader.file(new Path(
					fileName)), Reader.bufferSize(4096), Reader.start(0));
			Writable dkey = (Writable) ReflectionUtils.newInstance(
					reader.getKeyClass(), conf);
			Writable dvalue = (Writable) ReflectionUtils.newInstance(
					reader.getValueClass(), conf);
			while (reader.next(dkey, dvalue) && records-- > 0) {// 循环读取文件
				buffer.append(getValue(dkey)).append("\t")
						.append(getValue(dvalue)).append("<br>");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			IOUtils.closeStream(reader);
		}
		return buffer.toString();
	}

	/**
	 * 读取文本文件
	 * 
	 * @param fileName
	 * @param records
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static String readText(String fileName, int records)
			throws IllegalArgumentException, IOException {
		FileSystem fs = getFs();
		FSDataInputStream inStream = fs.open(new Path(fileName));
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		StringBuffer buffer = new StringBuffer();
		try {
			String line;
			line = br.readLine();
			while (line != null && records-- > 0) {
				// System.out.println(line);
				buffer.append(line).append("<br>");
				// be sure to read the next line otherwise you'll get an
				// infinite loop
				line = br.readLine();
			}
		} finally {
			// you should close out the BufferedReader
			br.close();
			inStream.close();
		}
		return buffer.toString();
	}

	public static synchronized void setCurrJob(Job currJob) {
		HUtils.currJob = currJob;
	}

	public static void setHadoopPassword(String hadoopPassword) {
		HUtils.hadoopPassword = hadoopPassword;
	}

	public static void setHadoopUserName(String hadoopUserName) {
		HUtils.hadoopUserName = hadoopUserName;
	}

	/**
	 * 设置同步方法
	 * 
	 * @param mrLock
	 */
	public static synchronized void setMrLock(MRLock mrLock) {
		HUtils.mrLock = mrLock;
	}

	/**
	 * 更新 绑定的hadoop用户密码
	 * 
	 * @param username
	 * @param password
	 */
	public static void updateHadoopUserNamePassword(String username,
			String password) {
		if (username != null) {
			hadoopUserName = username;
		} else {
			hadoopUserName = "null";
		}
		if (password != null) {
			setHadoopPassword(password);
		} else {
			setHadoopPassword("null");
		}
		initFs();// 重新初始化
	}

	public void cleanUp() {
		try {
			new File(JarUtil.getJarFilePath()).delete();
		} catch (Exception e) {
			log.info("删除" + JarUtil.getJarFilePath() + "异常!");
			e.printStackTrace();
			return;
		}
		log.info("jar 文件" + JarUtil.getJarFilePath() + "被删除！");
	}
}
