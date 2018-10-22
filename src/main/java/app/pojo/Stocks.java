package app.pojo;


import lombok.Data;

/**
 * 股票信息类
 *
 * @author huan
 */
@Data
public class Stocks {
	private int id ;
	private String code;
	private String name;
	private double price;
	private double chgPrice;
	private double chgPercent;
	// 单位: 手
	private double count;
	// 单位: 万
	private double money;

}
