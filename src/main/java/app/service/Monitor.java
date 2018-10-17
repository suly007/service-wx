package app.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.zhx.weixin.bean.Stocks;

public class Monitor extends Thread{
	//private String account_id="gh_ac538db20ea9";//正式号dgtz
	private String account_id="gh_69fae8b89eb0";//测试
	private DBServiceBO ds = new DBServiceBO();
	private List<Map<String, String>> stockMapListChg = ds.getStockMapListByAccountIdChg(account_id);
	private List<Map<String, String>> stockMapListComp = ds.getStockMapListByAccountIdComp(account_id);
	private List<Map<String, String>> blackMapList=ds.getBlackList();
	private String stockListStr = ds.getStockListStr();
	private String baseURL = "http://hq.sinajs.cn/";
	private MessageSend ms=new MessageSend();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");

	public void run(){
		int times = 0;

		
		Random r=new Random();
		while (true) {		
			Date now=new Date();
			int hour=now.getHours();
			if(hour<9||hour>15){
				try {
					DBServiceBO ds=new DBServiceBO();
					ds.dataInit();
					ds.delData();
					
					Thread.sleep(1000*60*30);
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			StringBuilder url=new StringBuilder();
			url.append(baseURL);
			url.append("rn=");
			url.append(r.nextLong());
			url.append("&list=");
			url.append(stockListStr);
			Map<String, Stocks> currentInfo = getCurrentInfo(url.toString());
			//chg process
			processChg(currentInfo);
			//comp process
			processComp(currentInfo);
			times++;
			//System.out.println(sdf.format(new Date())+" Monitor running ok....."+times);
			if(times==10){
				reLoadList();
				//delBlack();
				System.out.println(sdf.format(new Date())+" ok.....");
				times=0;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

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
		blackMapList.clear();
		stockMapListChg = ds.getStockMapListByAccountIdChg(account_id);
		stockMapListComp = ds.getStockMapListByAccountIdComp(account_id);
		blackMapList=ds.getBlackList();
		stockListStr = ds.getStockListStr();
		if(stockListStr==null||stockListStr.length()<=0){
			flag=false;
		}
		return flag;
	}
	
	public void processChg(Map<String, Stocks>  currentInfo){
		for (int i = 0; i < stockMapListChg.size(); i++) {
			StringBuilder msg = new StringBuilder();
			msg.append(sdf.format(new Date()));
			msg.append("\n");
			Map<String, String> stockMap = stockMapListChg.get(i);
			String stocks_id = stockMap.get("stocks_id");
			String stocks_alias = stockMap.get("stocks_alias");
			double change_min = Double.valueOf(stockMap.get("change_min"));
			double change_max = Double.valueOf(stockMap.get("change_max"));
			String open_id=stockMap.get("open_id");
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
				ms.SendMessage(msg.toString(),"text", open_id,true);
				if (ds.updateChange(stocks_id,"-")&&reLoadList()) {
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
				ms.SendMessage(msg.toString(),"text", open_id,true);
				if (ds.updateChange(stocks_id,"+")&&reLoadList()) {
					System.out.println("data process success....");
				} else {
					System.out.println("data process failed....");
				}
				break;//中断
			}
		}
	}
	
	public void processComp(Map<String, Stocks>  currentInfo){
		for(int i = 0; i < stockMapListComp.size(); i++) {
			StringBuilder msg = new StringBuilder();
			msg.append(sdf.format(new Date()));
			msg.append("\n");
			Map<String, String> stockMap = stockMapListComp.get(i);
			String stocks_id = stockMap.get("stocks_id");
			String stocks_alias = stockMap.get("stocks_alias");
			String stocks_alias_comp = stockMap.get("stocks_alias_comp");
			String open_id=stockMap.get("open_id");
			Stocks stocks = currentInfo.get(stocks_alias);
			Stocks stocks_comp = currentInfo.get(stocks_alias_comp);
		
			//如果比较代码为空或者比较代码的当前信息为空，跳出此次循环
			if(stocks_alias_comp==null||stocks_comp==null){
				continue;
			}			
			// 差异提示处理
			double base_diff = Double.valueOf(stockMap.get("base_diff"));
			double diff_range = Double.valueOf(stockMap.get("diff_range"));
			boolean diff_warn_flag = true;
			Date diff_warn_time=null;
			try {
				diff_warn_time = sdf.parse(stockMap.get("diff_warn_time"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			//System.out.println("diff_warn_time:"+sdf.format(diff_warn_time));
			long minutes = (new Date().getTime() - diff_warn_time.getTime()) / (1000 * 60);
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
						ms.SendMessage(msg.toString(),"text", open_id,true);
						if (ds.updateDiffWarnTime(stocks_id,"+")&&reLoadList()) {
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
						ms.SendMessage(msg.toString(),"text", open_id,true);
						if (ds.updateDiffWarnTime(stocks_id,"-")&&reLoadList()) {
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
