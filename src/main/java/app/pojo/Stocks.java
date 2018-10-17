package app.pojo;

public class Stocks {
	private String code;
	private String name;
	private double price;
	private double chgprice;
	private double chgpercent;
	private double count;//手
	private double money;//万
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getChgprice() {
		return chgprice;
	}
	public void setChgprice(double chgprice) {
		this.chgprice = chgprice;
	}
	public double getChgpercent() {
		return chgpercent;
	}
	public void setChgpercent(double chgpercent) {
		this.chgpercent = chgpercent;
	}
	public double getCount() {
		return count;
	}
	public void setCount(double count) {
		this.count = count;
	}
	public double getMoney() {
		return money;
	}
	public void setMoney(double money) {
		this.money = money;
	}
	

}
