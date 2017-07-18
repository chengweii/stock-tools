package stocktool;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;

import stocktool.StockTool.EnterpriseCurrentInfo.EnterpriseCurrentData;
import stocktool.StockTool.PlateInfo.PlateData;
import util.DBHelper;
import util.ExceptionUtil;
import util.GsonUtil;
import util.HttpUtil;
import util.StringUtil;

public class StockTool {

	private static Logger LOGGER = Logger.getLogger(HttpUtil.class);

	public static void main(String[] args) {
		synchronizeAllStockInfo();
	}

	private static void synchronizeAllStockInfo() {
		try {
			List<Map<String, Object>> stocklist = getStockList();

			Connection conn = DBHelper.getConn();

			String code = null;
			for (Map<String, Object> stock : stocklist) {
				code = String.valueOf(stock.get("code"));

				// synchronizeStockEnterpriseInfo(conn, code);
				synchronizeStockRealtimeInfo(conn, code);
				// synchronizeStockPlate(conn, code);
			}
			conn.close();
		} catch (Exception e) {
			ExceptionUtil.propagate(LOGGER, e);
		}
	}

	private static void synchronizeStockEnterpriseInfo(Connection conn, String code) {
		try {
			EnterpriseInfo enterpriseInfo = getEnterpriseInfo(code);
			if (enterpriseInfo != null) {
				String sql = "UPDATE `stock`.`enterprise_info` SET `ssdq`= ?, `zgb`= ?, `mgsy`= ?, `sssj`= ?, `ltga`= ?, `mgjzc`= ?, `mgxjl`= ?, `jzcsyl`= ?, `jlrzzl`= ?, `mgwfplr`= ?, `zysrzzl`= ?, `syl`= ? WHERE `stock_code`= ?";
				String[] params = { enterpriseInfo.data.ssdq, enterpriseInfo.data.zgb, enterpriseInfo.data.mgsy,
						enterpriseInfo.data.sssj, enterpriseInfo.data.ltga, enterpriseInfo.data.mgjzc,
						enterpriseInfo.data.mgxjl, enterpriseInfo.data.jzcsyl, enterpriseInfo.data.jlrzzl,
						enterpriseInfo.data.mgwfplr, enterpriseInfo.data.zysrzzl, code };
				DBHelper.excuteUpdate(conn, sql, params);
				LOGGER.info(code + " synchronize stockEnterpriseInfo success.");
				Thread.sleep(500);
			}
		} catch (Exception e) {
			ExceptionUtil.propagate(LOGGER, e);
		}
	}

	private static void initStockEnterpriseInfo(Connection conn, String code) {
		try {
			EnterpriseInfo enterpriseInfo = getEnterpriseInfo(code);
			if (enterpriseInfo != null) {
				String sql = "INSERT INTO `stock`.`enterprise_info` (`stock_code`,`ssdq`, `zgb`, `mgsy`, `sssj`, `ltga`, `mgjzc`, `mgxjl`, `jzcsyl`, `jlrzzl`, `mgwfplr`, `zysrzzl`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?)";
				String[] params = { code, enterpriseInfo.data.ssdq, enterpriseInfo.data.zgb, enterpriseInfo.data.mgsy,
						enterpriseInfo.data.sssj, enterpriseInfo.data.ltga, enterpriseInfo.data.mgjzc,
						enterpriseInfo.data.mgxjl, enterpriseInfo.data.jzcsyl, enterpriseInfo.data.jlrzzl,
						enterpriseInfo.data.mgwfplr, enterpriseInfo.data.zysrzzl };
				DBHelper.excuteUpdate(conn, sql, params);
				LOGGER.info(code + " init stockEnterpriseInfo success.");
				Thread.sleep(500);
			} else {
				String sql = "UPDATE `stock`.`stock_list` SET status = '0' WHERE code = ?";
				DBHelper.excuteUpdate(conn, sql, new String[] { code });
				Thread.sleep(200);
			}
		} catch (Exception e) {
			ExceptionUtil.propagate(LOGGER, e);
		}
	}

	private static void synchronizeStockPlate(Connection conn, String code) {
		try {
			PlateInfo plateInfo = getPlateInfo(code);
			if (plateInfo != null) {
				for (PlateData data : plateInfo.data) {
					String sql = "INSERT INTO `stock`.`stock_plate` (`stock_code`,`plate_name`, `plate_code`) VALUES (?, ?, ?)";
					String[] params = { code, data.name, data.code };
					DBHelper.excuteUpdate(conn, sql, params);
				}
				LOGGER.info(code + " synchronize stockPlate success.");
				Thread.sleep(500);
			}
		} catch (Exception e) {
			ExceptionUtil.propagate(LOGGER, e);
		}
	}

	private static void synchronizeStockRealtimeInfo(Connection conn, String code) {
		try {
			EnterpriseCurrentInfo enterpriseCurrentInfo = getEnterpriseCurrentInfo(code);
			if (enterpriseCurrentInfo != null) {
				String sql = "UPDATE `stock`.`stock_list` SET `status` = ? WHERE code = ?";
				EnterpriseCurrentData enterpriseCurrentData = enterpriseCurrentInfo.data.get(code);
				Object[] enterpriseData = enterpriseCurrentData.qt.get(code);
				String status = String.valueOf(enterpriseData[40]).trim();
				StockStatus stockStatus = StockStatus.fromValue(status);

				if (stockStatus != StockStatus.NORMAL) {
					String[] params = { stockStatus.code, code };
					DBHelper.excuteUpdate(conn, sql, params);
				} else {
					String sql2 = "UPDATE `stock`.`enterprise_info` SET `syl` = ? WHERE stock_code = ?";
					String[] params2 = { String.valueOf(enterpriseData[39]), code };
					DBHelper.excuteUpdate(conn, sql2, params2);
				}

				LOGGER.info(code + " synchronize stockRealtimeInfo success.");
				Thread.sleep(500);
			}
		} catch (Exception e) {
			ExceptionUtil.propagate(LOGGER, e);
		}
	}

	private static List<Map<String, Object>> getStockList() {
		try {
			Connection conn = DBHelper.getConn();
			String listsql = "SELECT * FROM `stock`.`stock_list` WHERE status = '1'";
			List<Map<String, Object>> stocklist = DBHelper.rsToMapList(DBHelper.excuteQuery(conn, listsql, null));
			conn.close();
			return stocklist;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static HistoryInfo getHistoryOfDayInfo(String stockCode, int dayCount) {
		String content = HttpUtil.get(
				"http://web.ifzq.gtimg.cn/appstock/app/fqkline/get?param=" + stockCode + ",day,,," + dayCount + ",qfq",
				null, 5000, 5000, "GBK");
		if (!StringUtil.isEmpty(content)) {
			HistoryInfo historyInfo = GsonUtil.getEntityFromJson(content, new TypeToken<HistoryInfo>() {
			});
			return historyInfo;
		}
		return null;
	}

	private static EnterpriseInfo getEnterpriseInfo(String stockCode) {
		String content = HttpUtil.get(
				"http://web.ifzq.gtimg.cn/appstock/hs/gszl/get?code=" + stockCode + "&type=gsgk_tips", null, 5000, 5000,
				"GBK");
		if (!StringUtil.isEmpty(content)) {
			StockData data = GsonUtil.getEntityFromJson(content, new TypeToken<StockData>() {
			});
			if (data != null && "0".equals(data.code)) {
				EnterpriseInfo enterpriseInfo = GsonUtil.getEntityFromJson(content, new TypeToken<EnterpriseInfo>() {
				});
				return enterpriseInfo;
			}
		}
		return null;
	}

	private static EnterpriseCurrentInfo getEnterpriseCurrentInfo(String stockCode) {
		String content = HttpUtil.get("http://web.ifzq.gtimg.cn/appstock/app/minute/query?code=" + stockCode, null,
				5000, 5000, "GBK");
		if (!StringUtil.isEmpty(content)) {
			StockData data = GsonUtil.getEntityFromJson(content, new TypeToken<StockData>() {
			});
			if (data != null && "0".equals(data.code)) {
				EnterpriseCurrentInfo enterpriseCurrentInfo = GsonUtil.getEntityFromJson(content,
						new TypeToken<EnterpriseCurrentInfo>() {
						});
				return enterpriseCurrentInfo;
			}
		}
		return null;
	}

	private static PlateInfo getPlateInfo(String stockCode) {
		String content = HttpUtil.get("http://web.ifzq.gtimg.cn/stock/relate/data/plate?code=" + stockCode, null, 5000,
				5000, "GBK");
		if (!StringUtil.isEmpty(content)) {
			PlateInfo plateInfo = GsonUtil.getEntityFromJson(content, new TypeToken<PlateInfo>() {
			});
			return plateInfo;
		}
		return null;
	}

	private static PlateInfo getStockRealTimeInfo(String stockCode) {
		String content = HttpUtil.get("http://web.ifzq.gtimg.cn/stock/relate/data/plate?code=" + stockCode, null, 5000,
				5000, "GBK");
		if (!StringUtil.isEmpty(content)) {
			PlateInfo plateInfo = GsonUtil.getEntityFromJson(content, new TypeToken<PlateInfo>() {
			});
			return plateInfo;
		}
		return null;
	}

	public static class HistoryInfo {
		public String code;
		public String msg;
		public Map<String, HistoryData> data;

		public static class HistoryData {
			public List<List<Object>> qfqday;
			public Object qt;
			public Object mx_price;
			public Object prec;
			public Object version;
		}
	}

	public static class PlateInfo {
		public String code;
		public String msg;
		public List<PlateData> data;

		public static class PlateData {
			public String code;
			public String name;
		}
	}

	public static class EnterpriseCurrentInfo {
		public String code;
		public String msg;
		public Map<String, EnterpriseCurrentData> data;

		public static class EnterpriseCurrentData {
			public Map<String, Object[]> qt;
		}
	}

	public static class EnterpriseInfo {
		public String code;
		public String msg;
		public EnterpriseData data;

		public static class EnterpriseData {
			// 所属地区
			public String ssdq;
			// 总股本(亿)
			public String zgb;
			// 每股收益(元)
			public String mgsy;
			// 上市时间
			public String sssj;
			// 流通A股(亿)
			public String ltga;
			// 每股净资产(元)
			public String mgjzc;
			// 每股现金流(元)
			public String mgxjl;
			// 每股公积金(元)
			public String mggjj;
			// 净资产收益率(%)
			public String jzcsyl;
			// 净利润增长率(%)
			public String jlrzzl;
			// 每股未分配利润
			public String mgwfplr;
			// 主营收入增长率(%)
			public String zysrzzl;
		}
	}

	static class StockData {
		public String code;
		public String msg;
		public Object data;
	}

	static enum StockStatus {
		NORMAL("1", ""), STOP("2", "S"), EXIT("3", "D");

		private StockStatus(String code, String value) {
			this.code = code;
			this.value = value;
		}

		private String code;
		private String value;

		public String getCode() {
			return code;
		}

		public String getValue() {
			return value;
		}

		public static StockStatus fromCode(String code) {
			for (StockStatus entity : StockStatus.values()) {
				if (entity.getCode().equals(code)) {
					return entity;
				}
			}
			return NORMAL;
		}

		public static StockStatus fromValue(String value) {
			for (StockStatus entity : StockStatus.values()) {
				if (entity.getValue().equals(value)) {
					return entity;
				}
			}
			return NORMAL;
		}
	}

}
