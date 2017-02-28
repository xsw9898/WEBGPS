package com.gps808.monitor.dao.impl;

import com.framework.web.dao.HibernateDaoSupportEx;
import com.framework.web.dto.QueryScalar;
import com.gps.model.DeviceStatusLite;
import com.gps808.model.StandardAlarmMotion;
import com.gps808.model.StandardFixedTts;
import com.gps808.monitor.dao.StandardMonitorDao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class StandardMonitorDaoHibernate
  extends HibernateDaoSupportEx
  implements StandardMonitorDao
{
  public StandardFixedTts getStandardFixedTts(Integer userId, String content)
  {
    List<StandardFixedTts> tts = getHibernateTemplate().find(String.format("from StandardFixedTts where userId = %d and content = '%s'", new Object[] { userId, content }));
    if ((tts != null) && (tts.size() > 0)) {
      return (StandardFixedTts)tts.get(0);
    }
    return null;
  }
  
  public List<StandardFixedTts> getStandardFixedTts(Integer userId)
  {
    Query query = getSession().createQuery(String.format("from StandardFixedTts where userId = %d", new Object[] { userId }));
    if (query == null) {
      return null;
    }
    return query.list();
  }
  
  public StandardAlarmMotion findAlarmMotion(Integer userId, String vehiIdno, Integer armType)
  {
    List<StandardAlarmMotion> alarmMotions = getHibernateTemplate().find(String.format(" from StandardAlarmMotion where uid = %d and vid = '%s' and atp = %d", new Object[] { userId, vehiIdno, armType }));
    if ((alarmMotions != null) && (alarmMotions.size() >= 1)) {
      return (StandardAlarmMotion)alarmMotions.get(0);
    }
    return null;
  }
  
  public Map<Integer, StandardAlarmMotion> findAlarmMotion(Integer userId, String vehiIdno)
  {
    List<StandardAlarmMotion> alarmMotions = getHibernateTemplate().find(String.format(" from StandardAlarmMotion where uid = %d and vid = '%s' ", new Object[] { userId, vehiIdno }));
    if ((alarmMotions != null) && (alarmMotions.size() >= 1))
    {
      Map<Integer, StandardAlarmMotion> mapMotion = new HashMap();
      for (int i = 0; i < alarmMotions.size(); i++) {
        mapMotion.put(((StandardAlarmMotion)alarmMotions.get(i)).getAtp(), (StandardAlarmMotion)alarmMotions.get(i));
      }
      return mapMotion;
    }
    return null;
  }
  
  private StringBuffer getQueryStringBuffer()
  {
    StringBuffer sql = new StringBuffer();
    sql.append("select DevIDNO as id,GWSvrIDNO as gw,Network as net,Online as ol,Status1 as s1,");
    sql.append("Status2 as s2,Status3 as s3,Status4 as s4,YouLiang as yl,Speed as sp,HangXiang as hx,");
    sql.append("JingDu as lng,WeiDu as lat,ParkTime as pk,LiCheng as lc,GPSTime as gt,Protocol as pt,");
    sql.append("TempSensor1 as t1,TempSensor2 as t2,TempSensor3 as t3,TempSensor4 as t4,DiskType as dt,AudioCodec as ac,");
    sql.append("FactoryType as ft,FactoryDevType as fdt,");
    sql.append("LineID as lid,DriverID as drid,LineDirection as dct,StationFlag as sfg,");
    sql.append("StationIndex as snm,StationStatus as sst,");
    sql.append("ObdRpm as 'or',ObdSpeed as os,ObdVotage as ov,ObdJQTemp as ojt,");
    sql.append("ObdStatus as ost,ObdJQMPos as ojm from dev_status where 1 = 1");
    return sql;
  }
  
  private List<QueryScalar> getDeviceStatusLiteQueryScalar()
  {
    List<QueryScalar> scalars = new ArrayList();
    scalars.add(new QueryScalar("id", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("s1", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("s2", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("s3", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("s4", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("t1", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("t2", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("t3", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("t4", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("yl", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("sp", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("hx", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("lng", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("lat", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("pk", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("lc", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("gt", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("gw", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("net", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ol", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("pt", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("dt", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ac", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ft", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("fdt", StandardBasicTypes.INTEGER));
    
    scalars.add(new QueryScalar("lid", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("drid", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("dct", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("sfg", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("snm", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("sst", StandardBasicTypes.INTEGER));
    
    scalars.add(new QueryScalar("or", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("os", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ov", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ojt", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ost", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("ojm", StandardBasicTypes.INTEGER));
    return scalars;
  }
  
  public List<DeviceStatusLite> getDeviceStatusLite(String[] devIdnos)
    throws HibernateException
  {
    StringBuffer strQuery = getQueryStringBuffer();
    if (devIdnos.length > 0)
    {
      strQuery.append(" and DevIDNO in(?");
      int i = 1;
      for (int j = devIdnos.length; i < j; i++) {
        strQuery.append(",?");
      }
      strQuery.append(")");
    }
    List<QueryScalar> scalars_ = getDeviceStatusLiteQueryScalar();
    Session session = getHibernateTemplate().getSessionFactory().openSession();
    SQLQuery query = null;
    try
    {
      query = session.createSQLQuery(strQuery.toString());
      if (devIdnos.length > 0)
      {
        int i = 0;
        for (int j = devIdnos.length; i < j; i++) {
          query.setString(i, devIdnos[i]);
        }
      }
      for (int i = 0; i < scalars_.size(); i++) {
        query.addScalar(((QueryScalar)scalars_.get(i)).getValue(), ((QueryScalar)scalars_.get(i)).getType());
      }
      query.setResultTransformer(Transformers.aliasToBean(DeviceStatusLite.class));
      return query.list();
    }
    catch (RuntimeException re)
    {
      throw re;
    }
    finally
    {
      session.close();
    }
  }
}
