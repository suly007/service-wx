package app.service;

import app.pojo.Stocks;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Monitor extends Thread{
	@Value("${weixin.appid}")
	private String appid;

	private DataService dataService;
	private List<Map<String, Object>> stockMapListChg ;
	private List<Map<String, Object>> stockMapListComp;
	//private List<Map<String, String>> blackMapList=dataService.getBlackList();
	private String stockListStr;
	private String baseURL = "http://hq.sinajs.cn/";

	@Autowired
	private QywxMessageService qywxMessageService;

	private SimpleDateFormat simpleDateFormat ;
	private static final java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");


	@Autowired
	public Monitor(DataService dataService,SimpleDateFormat simpleDateFormat){
		this.dataService=dataService;
		this.simpleDateFormat =simpleDateFormat;
		stockMapListChg = dataService.getStockMapListByAppidChg(appid);
		stockMapListComp = dataService.getStockMapListByAppidComp(appid);
		stockListStr = dataService.getStockListStr();
	}


	//public void


	@Override
	public void run(){


	}


	public Map<String, Stocks> getAllCurrentInfo(){
		StringBuilder url=new StringBuilder();
		url.append(baseURL);
		url.append("rn=");
		url.append(RandomUtils.nextLong());
		url.append("&list=");
		url.append(stockListStr);
		return getCurrentInfo(url.toString());
	}

	public Map<String, Stocks> getCurrentInfo(String strUrl) {
//		System.out.println("发送请求地址："+strUrl);
		Map<String, Stocks> resultMap = new HashMap<String, Stocks>();
		StringBuilder contentBuf = new StringBuilder();
		try{
		URL url = new URL(strUrl);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.connect();
		InputStreamReader input = new InputStreamReader(httpConn.getInputStream(), "gbk");
		BufferedReader bufReader = new BufferedReader(input);
		String line = "";
		while ((line = bufReader.readLine()) != null) {
			contentBuf.append(line);
		}
		bufReader.close();
		httpConn.disconnect();
		}catch(Exception e){
			System.out.println("链接异常");
		}
//		System.out.println("contentBuf"+contentBuf);
		String[] results = contentBuf.toString().split(";");
		for (int i = 0; i < results.length; i++) {
			Stocks stocks = new Stocks();
			String[] keyValue = results[i].split("=");
			String key = keyValue[0].replace("var hq_str_", "");
			String valuestr=keyValue[1].replace("\"", "");
//			System.out.println("valuestr:"+valuestr);
			String[] values = valuestr.split(",");
//			System.out.println("values:长度"+values.length);
			stocks.setName(values[0]);
			stocks.setPrice(Double.valueOf(values[1]));
			stocks.setChgprice(Double.valueOf(values[2]));
			stocks.setChgpercent(Double.valueOf(values[3]));
			stocks.setCount(Double.valueOf(values[4]));
			stocks.setMoney(Double.valueOf(values[5]));
			stocks.setCode(key.replace("s_sh", "").replace("s_sz", ""));
			resultMap.put(key, stocks);
		}

		return resultMap;

	}
//	//删除黑名
//	public void delBlack(){
//		for(int i=0;i<blackMapList.size();i++){
//			String open_id=blackMapList.get(i).get("open_id");
//			ms.moveUser(open_id,"1");
//		}
//	}
	
	//重新加载列表
	public boolean reLoadList(){
		boolean flag=true;
		System.out.println("reLoadList......");
		stockMapListComp.clear();
		stockMapListChg.clear();	
		//blackMapList.clear();
		stockMapListChg = dataService.getStockMapListByAppidChg(appid);
		stockMapListComp = dataService.getStockMapListByAppidComp(appid);
		//blackMapList=ds.getBlackList();
		stockListStr = dataService.getStockListStr();
		if(stockListStr==null||stockListStr.length()<=0){
			flag=false;
		}
		return flag;
	}

	/**
	 * 涨跌幅处理
	 *
	 * @param currentInfo
	 */
	public void processChg(Map<String, Stocks>  currentInfo){
		for (int i = 0; i < stockMapListChg.size(); i++) {
			StringBuilder msg = new StringBuilder();
			msg.append(simpleDateFormat.format(new Date()));
			msg.append("\n");
			Map<String, Object> stockMap = stockMapListChg.get(i);
			String stocks_id = MapUtils.getString(stockMap,"stocks_id");
			String stocks_alias = MapUtils.getString(stockMap,"stocks_alias");
			double change_min = MapUtils.getDouble(stockMap,"change_min");
			double change_max = MapUtils.getDouble(stockMap,"change_max");
			String open_id=  MapUtils.getString(stockMap,"open_id");
			Stocks stocks = currentInfo.get(stocks_alias);		
			if (stocks.getChgpercent() < change_min) {
				// 发送跌幅提示
				System.out.println("change_min.warn.............");
				msg.append(stocks.getName());
				msg.append("下跌提示,\n涨幅:");
				double chgpercent=stocks.getChgpercent();
				if(chgpercent>0){
					msg.append("+");
				}
				msg.append(chgpercent);
				msg.append("%\n");
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" >分时</a>");
				
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/daily/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" > 日K</a>");
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/weekly/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" > 周K</a>");
				
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/monthly/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" > 月K</a>");

				Message message = new Message("100012",msg.toString(),"text",open_id,true);

				qywxMessageService.SendMessage(message);
				if (dataService.updateChange(stocks_id,"-")&&reLoadList()) {
					System.out.println("data process success....");
				} else {
					System.out.println("data process failed....");
				}
				break;//中断
			}
			if (stocks.getChgpercent() > change_max) {
				// 发送涨幅提示
				System.out.println("change_max.warn.............");
				msg.append(stocks.getName());
				msg.append("上涨提示,\n涨幅:");
				double chgpercent=stocks.getChgpercent();
				if(chgpercent>0){
					msg.append("+");
				}
				msg.append(chgpercent);
				msg.append("%\n");
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" >分时</a>");
				
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/daily/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" > 日K</a>");
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/weekly/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" > 周K</a>");
				
				msg.append(" <a href=\"http://image.sinajs.cn/newchart/monthly/n/");
				msg.append(stocks_alias.replace("s_", ""));
				msg.append(".gif\" > 月K</a>");
				Message message = new Message("100012",msg.toString(),"text",open_id,true);

				qywxMessageService.SendMessage(message);
				if (dataService.updateChange(stocks_id,"+")&&reLoadList()) {
					System.out.println("data process success....");
				} else {
					System.out.println("data process failed....");
				}
				break;//中断
			}
		}
	}


	/**
	 * 对比处理
	 * @param currentInfo
	 */
	public void processComp(Map<String, Stocks>  currentInfo){
		for(int i = 0; i < stockMapListComp.size(); i++) {
			StringBuilder msg = new StringBuilder();
			msg.append(simpleDateFormat.format(new Date()));
			msg.append("\n");
			Map<String, Object> stockMap = stockMapListComp.get(i);
			String stocks_id = MapUtils.getString(stockMap,"stocks_id");
			String stocks_alias = MapUtils.getString(stockMap,"stocks_alias");
			String stocks_alias_comp = MapUtils.getString(stockMap,"stocks_alias_comp");
			String open_id=MapUtils.getString(stockMap,"open_id");
			Stocks stocks = currentInfo.get(stocks_alias);
			Stocks stocks_comp = currentInfo.get(stocks_alias_comp);
		
			//如果比较代码为空或者比较代码的当前信息为空，跳出此次循环
			if(stocks_alias_comp==null||stocks_comp==null){
				continue;
			}			
			// 差异提示处理
			double base_diff = MapUtils.getDouble(stockMap,"base_diff");
			double diff_range = MapUtils.getDouble(stockMap,"diff_range");
			boolean diff_warn_flag = true;
			Date diff_warn_time=null;
			diff_warn_time = (Date)MapUtils.getObject(stockMap,"diff_warn_time");
			//System.out.println("diff_warn_time:"+sdf.format(diff_warn_time));
			long minutes = (System.currentTimeMillis() - diff_warn_time.getTime()) / (1000 * 60);
			// 小于5分钟不进行差异提示
			if (minutes < 5) {
				diff_warn_flag = false;
			}
			//System.out.println("minutes:"+minutes+"diff_warn_flag:"+diff_warn_flag);
			if (diff_warn_flag&&stocks_comp!=null) {
				double stocks_price = stocks.getPrice();
				double stocks_price_comp = stocks_comp.getPrice();
				if (stocks_price > 0 && stocks_price_comp > 0) {
					double realDiff = (stocks_price - stocks_price_comp) /stocks_price *100;
					if (realDiff > (base_diff + diff_range)) {
						// 提示
						System.out.println("sale.warn.............");
						msg.append("差异:");
						msg.append(df.format(realDiff));
						msg.append("%\n");
						msg.append("卖出:");
						msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/" );					
						msg.append(stocks_alias.replace("s_", ""));
						msg.append(".gif\" >");
						//msg.append(stocks.getCode());
						msg.append(stocks.getName());
						msg.append("</a>");
						msg.append(stocks.getPrice());
						msg.append(",");
						msg.append(stocks.getChgpercent());
						msg.append("%\n");
						msg.append("买入:");
						msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/" );					
						msg.append(stocks_alias_comp.replace("s_", ""));
						msg.append(".gif\" >");
						//msg.append(stocks_comp.getCode());
						msg.append(stocks_comp.getName());
						msg.append("</a>");
						msg.append(stocks_comp.getPrice());
						msg.append(",");
						msg.append(stocks_comp.getChgpercent());
						msg.append("% \n");
						Message message = new Message("100012",msg.toString(),"text",open_id,true);
						qywxMessageService.SendMessage(message);
						if (dataService.updateDiffWarnTime(stocks_id,"+")&&reLoadList()) {
							System.out.println("data process success....");
						} else {
							System.out.println("data process failed....");
						}							
						break;
					}
					//System.out.println("base_diff  diff_range"+(base_diff - diff_range));
					if (realDiff < (base_diff - diff_range)) {
						// 提示
						System.out.println("buy.warn.............");
						msg.append("差异:");
						msg.append(df.format(realDiff));
						msg.append("%,\n");
						msg.append("卖出:");
						msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/" );					
						msg.append(stocks_alias_comp.replace("s_", ""));
						msg.append(".gif\" >");
						//msg.append(stocks_comp.getCode());
						msg.append(stocks_comp.getName());
						msg.append("</a>");
						msg.append(stocks_comp.getPrice());
						msg.append(",");
						msg.append(stocks_comp.getChgpercent());
						msg.append("% \n");
						msg.append("买入:");
						msg.append(" <a href=\"http://image.sinajs.cn/newchart/min/n/" );					
						msg.append(stocks_alias.replace("s_", ""));
						msg.append(".gif\" >");
						//msg.append(stocks.getCode());
						msg.append(stocks.getName());
						msg.append("</a>");
						msg.append(stocks.getPrice());
						msg.append(",");
						msg.append(stocks.getChgpercent());
						msg.append("%\n");
						Message message = new Message("100012",msg.toString(),"text",open_id,true);
						qywxMessageService.SendMessage(message);
						if (dataService.updateDiffWarnTime(stocks_id,"-")&&reLoadList()) {
							System.out.println("data process success....");			
						} else {
							System.out.println("data process failed....");
						}
						break;
					}
				}
			}

		}
	}
}
