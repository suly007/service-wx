package app.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据访问服务
 *
 * @author faith.huan 2018-10-23 11:43:27
 */
@Service
@Slf4j
public class DataService {
    private static final long serialVersionUID = 1L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //通过公众号，获取用户查询过的代码列表信息
    public List<Map<String, Object>> getStockMapListByCorpIdChg(String appid) {
        String sql = "select stocks_code, stocks_alias, stocks_code_comp, stocks_alias_comp, stocks_price_init, stocks_price_init_comp,diff_warn_process_flag, ifnull(diff_warn_time,now()) diff_warn_time, diff_range, change_min, change_max, stocks_id, open_id, add_date, modify_date from stocks_list where appid=? ";
        return jdbcTemplate.queryForList(sql, appid);
    }

    //通过公众号，获取用户查询过的代码列表信息
    public List<Map<String, Object>> getStockMapListByCorpIdComp(String appid) {
        String sql = "select stocks_code, stocks_alias, stocks_code_comp, stocks_alias_comp, base_multiple, stocks_price_init, stocks_price_init_comp,diff_warn_process_flag, ifnull(diff_warn_time,now()) diff_warn_time, diff_range, change_min, change_max, stocks_id, open_id,  add_date, modify_date from stocks_list where appid=? and stocks_code_comp is not null";
        return jdbcTemplate.queryForList(sql, appid);
    }

    public String getStockListStr() {
        String sql = "select GROUP_CONCAT(x.stocks_alias) stocks_alias from (select distinct t1.stocks_alias as stocks_alias  from stocks_list t1 where t1.stocks_alias is not null union select distinct t2.stocks_alias_comp  from stocks_list t2 where t2.stocks_alias_comp is not null ) x";
        return MapUtils.getString(jdbcTemplate.queryForMap(sql), "stocks_alias");
    }

    public String getStockListStrByOpenID(String open_id) {
        String sql = "select GROUP_CONCAT(x.stocks_alias) stocks_alias from (select distinct t1.stocks_alias as stocks_alias  from stocks_list t1 where t1.stocks_alias is not null and t1.open_id=? union select distinct t2.stocks_alias_comp  from stocks_list t2 where t2.stocks_alias_comp is not null and t2.open_id=? ) x";
        return MapUtils.getString(jdbcTemplate.queryForMap(sql, open_id, open_id), "stocks_alias");
    }

    public boolean updateDiffWarnTime(int stocks_id) {
        String sql = "update stocks_list set diff_warn_time=now(),diff_warn_process_flag='N' where stocks_id =?";
        return jdbcTemplate.update(sql, stocks_id) > 0;
    }

    /**
     * 修改比价初始价格,并将预警处理状态改为已处理
     *
     * @param open_id           微信号
     * @param appId             企业号
     * @param priceInit        初始价格
     * @param priceInitComp    初始价格-对比
     * @return
     */
    public int chgInitPriceByUser(String open_id, String appId, double priceInit, double priceInitComp) {
        String sql = "update stocks_list set stocks_price_init=?,stocks_price_init_comp=?,diff_warn_process_flag='Y'  where open_id=? and appid=? and  diff_warn_process_flag='N'";
        return jdbcTemplate.update(sql, priceInit, priceInitComp, open_id, appId);
    }

    /**
     * 修改比价初始价格,并将预警处理状态改为已处理
     *
     * @param open_id           微信号
     * @param appId             企业号
     * @return
     */
    public int chgInitPriceByUser(String open_id, String appId) {
        String sql = "update stocks_list set diff_warn_process_flag='Y'  where open_id=? and appid=? and  diff_warn_process_flag='N'";
        return jdbcTemplate.update(sql, open_id, appId);
    }

    public boolean updateChange(int stocks_id, String flag) {
        String sql = "update stocks_list set change_min=(change_min" + flag + "2),change_max=(change_max" + flag + "2) where stocks_id=? ";
        return jdbcTemplate.update(sql, stocks_id) > 0;
    }


    public int initData() {
        String sql = "update stocks_list set diff_warn_time=now()-1,change_max=2,change_min=-2 ";
        return jdbcTemplate.update(sql);
    }

    public boolean notExist(String stocks_code, String open_id, String appid) {
        String sql = "select 1 from  stocks_list where stocks_code=? and open_id=? and appid=? and stocks_code_comp is null";
        return jdbcTemplate.queryForList(sql, stocks_code, open_id, appid).size() == 0;
    }

    public boolean delExist(String stocks_code, String open_id, String appid) {
        String sql = "delete from  stocks_list where stocks_code=? and open_id=? and appid=? and stocks_code_comp is null";
        return jdbcTemplate.update(sql, stocks_code, open_id, appid) > 0;
    }

    public boolean delAllExist(String open_id, String appid) {
        String sql = "delete from  stocks_list where open_id=? and appid=? and stocks_code_comp is null";
        return jdbcTemplate.update(sql, open_id, appid) > 0;
    }

    public boolean insertData(String stocks_code, String stocks_alias, String open_id, String appid) {
        String sql = "insert into stocks_list   (stocks_code, stocks_alias, open_id,appid) values (?,?,?,?)";
        return jdbcTemplate.update(sql, stocks_code, stocks_alias, open_id, appid) > 0;
    }


    /*public boolean updateBaseDiff(String num, String open_id, String appid, String stocks_code) {
        String sql = "update stocks_list t set t.base_diff=? where opne_id=? and appid=? and  stocks_code=?";

        return jdbcTemplate.update(sql, num, open_id, appid, stocks_code) > 0;
    }*/

    //十日内无查询数据代码 删除
    public boolean delData() {
        String delStocks = "delete from  stocks_list where open_id in (select open_id from weixin_blacklist)";
        return jdbcTemplate.update(delStocks) > 0;
    }

    /**
     * weixin_token 表处理=========================================================================
     */
    //获取未过期的
    public Map<String, Object> getToken(String appid, String appsecret) {
        String sql = "select appid, appsecret, token, expiresin, expiresdate from weixin_token where appid=? and appsecret=? and expiresdate>now() order by add_date desc limit 1";
        try {
            return jdbcTemplate.queryForMap(sql, appid, appsecret);
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<>();
        }
    }

    //设置token过期
    public boolean setTokenExpiresd(String appid, String appsecret) {
        String sql = "update weixin_token set expiresdate=now()-(1/24/60) where appid=? and appsecret=?  and expiresdate>now() ";
        return jdbcTemplate.update(sql, appid, appsecret) > 0;
    }

    public boolean insertToken(String appid, String appsecret, String token, String expiresin, String expiresdate) {
        log.info("save token to db......");
        String sql = "insert into weixin_token   (appid, appsecret, token, expiresin, expiresdate) values (?,?,?,?,?)";

        return jdbcTemplate.update(sql, appid, appsecret, token, expiresin, expiresdate) > 0;
    }


    public boolean insertMessage(String open_id, String user, String message, String res) {
        String sql = "insert into weixin_message ( to_open_id, to_user, message,send_res, send_time) values (?,?,?,?,now())";
        return jdbcTemplate.update(sql, open_id, user, message, res) > 0;
    }

    public List<Map<String, Object>> getBlackList() {
        String sql = "select * from weixin_blacklist";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * weixin_errorinfo 表处理=========================================================================
     */
    public boolean insertErrorInfo(String className, String methodName, String errorInfo) {
        String sql = "insert into weixin_errorinfo(class_name,method_name,error_info,error_date,add_date) values (?,?,?,now(),now())";
        /*DaoParam param = _dao.createParam(sql);
        param.addStringValue(className);
        param.addStringValue(methodName);
        param.addStringValue(errorInfo);
        return _dao.update(param);*/
        return true;
    }
}
