package com.gps808.report.service.impl;

import com.framework.utils.ByteArrayUtils;
import com.framework.utils.DateUtil;
import com.framework.web.dao.PaginationDao;
import com.framework.web.dto.AjaxDto;
import com.framework.web.dto.Pagination;
import com.framework.web.dto.QueryScalar;
import com.framework.web.service.impl.UniversalServiceImpl;
import com.gps.util.ConvertUtil;
import com.gps.vo.GpsValue;
import com.gps808.model.StandardDeviceGps;
import com.gps808.model.StandardTransportGps;
import com.gps808.report.service.StandardVehicleGpsService;
import com.gps808.report.vo.DeviceAlarmCompare;
import com.gps808.report.vo.StandardDeviceAlarmEx;
import com.gps808.report.vo.StandardDeviceAlarmSummary;
import com.gps808.report.vo.StandardDeviceMinMaxGps;
import com.gps808.report.vo.StandardDeviceTrack;
import com.gps808.report.vo.StandardTpmsTrack;
import com.gps808.report.vo.StandardTpmsTrackCompare;
import com.gps808.report.vo.StandardTrackCompare;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.hibernate.type.StandardBasicTypes;

public class StandardVehicleGpsServiceImpl
  extends UniversalServiceImpl
  implements StandardVehicleGpsService
{
  private PaginationDao paginationDao;
  
  public Class getClazz()
  {
    return StandardDeviceGps.class;
  }
  
  public PaginationDao getPaginationDao()
  {
    return this.paginationDao;
  }
  
  public void setPaginationDao(PaginationDao paginationDao)
  {
    this.paginationDao = paginationDao;
  }
  
  public static Date analyTrackTime(long time)
  {
    byte year = (byte)(int)(time & 0x3F);
    byte month = (byte)(int)(time >> 6 & 0xF);
    byte day = (byte)(int)(time >> 10 & 0x1F);
    byte hour = (byte)(int)(time >> 15 & 0x1F);
    byte minute = (byte)(int)(time >> 20 & 0x3F);
    byte second = (byte)(int)(time >> 26 & 0x3F);
    Calendar c = Calendar.getInstance();
    c.set(2000 + year, month - 1, day, hour, minute, second);
    
    return c.getTime();
  }
  
  public static void analyDeviceStatus(int status, StandardDeviceTrack track)
  {
    track.setIsParking(Boolean.valueOf((status >> 13 & 0x1) == 1));
  }
  
  public static void analyDeviceTrack(byte[] data, int offset, StandardDeviceTrack track)
  {
    offset += 4;
    int temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    
    track.setSpeed(Integer.valueOf(temp & 0x3FFF));
    
    track.setYouLiang(Integer.valueOf(temp >> 14 & 0x3FFFF));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setLiCheng(Integer.valueOf(temp));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setHuangXiang(Integer.valueOf(temp & 0x1FF));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    track.setStatus1(Integer.valueOf(temp));
    offset += 4;
    analyDeviceStatus(temp, track);
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    track.setStatus2(Integer.valueOf(temp));
    offset += 4;
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    track.setStatus3(Integer.valueOf(temp));
    offset += 4;
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    track.setStatus4(Integer.valueOf(temp));
    offset += 4;
    
    short sTemp = ByteArrayUtils.byteArray2short(data, offset);
    offset += 2;
    track.setTempSensor1(Integer.valueOf(sTemp));
    
    sTemp = ByteArrayUtils.byteArray2short(data, offset);
    offset += 2;
    track.setTempSensor2(Integer.valueOf(sTemp));
    
    sTemp = ByteArrayUtils.byteArray2short(data, offset);
    offset += 2;
    track.setTempSensor3(Integer.valueOf(sTemp));
    
    sTemp = ByteArrayUtils.byteArray2short(data, offset);
    offset += 2;
    track.setTempSensor4(Integer.valueOf(sTemp));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setJingDu(Integer.valueOf(temp));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setWeiDu(Integer.valueOf(temp));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setGaoDu(Integer.valueOf(temp));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setParkTime(Integer.valueOf(temp));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    
    track.setLineID(Integer.valueOf(temp & 0x1FFFFFFF));
    
    track.setDirection(Integer.valueOf(temp >> 29 & 0x1));
    
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    
    track.setOdbVotage(Integer.valueOf(temp & 0x1FF));
    
    track.setOdbJQTemp(Integer.valueOf(temp >> 9 & 0xFF));
    
    track.setOdbStatus(Integer.valueOf(temp >> 17 & 0x7F));
    temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    
    track.setObdRpm(Integer.valueOf(temp & 0x3FFF));
    
    track.setObdSpeed(Integer.valueOf(temp >> 14 & 0xFF));
    
    track.setOdbJQMPos(Integer.valueOf(temp >> 22 & 0x3FF));
  }
  
  public static void analyTpmsTrack(byte[] data, int offset, StandardTpmsTrack track)
  {
    offset += 4;
    int temp = ByteArrayUtils.byteArray2int(data, offset);
    offset += 4;
    track.setTireCount(Integer.valueOf(temp & 0xFF));
    List<Integer> battery = new ArrayList();
    List<Integer> press = new ArrayList();
    List<Integer> temperure = new ArrayList();
    for (int i = 0; i < 20; i++)
    {
      temp = ByteArrayUtils.byteArray2int(data, offset);
      offset += 4;
      battery.add(Integer.valueOf(temp & 0xFF));
      press.add(Integer.valueOf(temp >> 8 & 0xFF));
      temperure.add(Integer.valueOf(temp >> 16 & 0xFFFF));
    }
    track.setTireBatterys(battery);
    track.setTirePress(press);
    track.setTemperures(temperure);
  }
  
  public void analyDeviceGps(StandardDeviceGps gps, String vehiIdno, long beginTime, long endTime, List<StandardDeviceTrack> gpstracks, String toMap)
  {
    try
    {
      InputStream inStream = gps.getGpsData().getBinaryStream();
      long nLen = gps.getGpsData().length();
      int nSize = (int)nLen;
      byte[] data = new byte[nSize];
      inStream.read(data);
      inStream.close();
      
      int count = nSize / 72;
      if (count > 0)
      {
        int offset = 0;
        for (int i = 0; i < count; i++)
        {
          long date = ByteArrayUtils.byteArray2long(data, offset, 4);
          Date gpsTime = analyTrackTime(date);
          
          long gpsSecond = gpsTime.getTime();
          if ((gpsSecond >= beginTime) && (gpsSecond <= endTime))
          {
            StandardDeviceTrack track = new StandardDeviceTrack();
            track.setTrackTime(gpsSecond);
            track.setVehiIdno(vehiIdno);
            track.setPlateType(gps.getPlateType());
            track.setDevIdno(gps.getDevIdno());
            track.setGpsTime(DateUtil.dateSwitchString(gpsTime));
            track.setGpsTimeStr(DateUtil.dateSwitchString(gpsTime));
            analyDeviceTrack(data, offset, track);
            if ((track.getStatus1().intValue() & 0x1) > 0)
            {
              GpsValue gpsValue = ConvertUtil.convert(track.getJingDu(), track.getWeiDu(), toMap);
              
              track.setMapJingDu(gpsValue.getMapJingDu());
              track.setMapWeiDu(gpsValue.getMapWeiDu());
            }
            gpstracks.add(track);
          }
          offset += 72;
        }
      }
      data = null;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public void analyTpmsGps(StandardTransportGps gps, String vehiIdno, long beginTime, long endTime, List<StandardTpmsTrack> gpstracks, String toMap)
  {
    try
    {
      InputStream inStream = gps.getTransportData().getBinaryStream();
      long nLen = gps.getTransportData().length();
      int nSize = (int)nLen;
      byte[] data = new byte[nSize];
      inStream.read(data);
      inStream.close();
      
      int count = nSize / 88;
      if (count > 0)
      {
        int offset = 0;
        for (int i = 0; i < count; i++)
        {
          long date = ByteArrayUtils.byteArray2long(data, offset, 4);
          Date gpsTime = analyTrackTime(date);
          long gpsSecond = gpsTime.getTime();
          if ((gpsSecond >= beginTime) && (gpsSecond <= endTime))
          {
            StandardTpmsTrack track = new StandardTpmsTrack();
            track.setTrackTime(gpsSecond);
            track.setVehiIdno(vehiIdno);
            track.setPlateType(gps.getPlateType());
            track.setType(gps.getType());
            track.setDevIdno(gps.getDevIdno());
            track.setGpsTime(DateUtil.dateSwitchString(gpsTime));
            track.setGpsTimeStr(DateUtil.dateSwitchString(gpsTime));
            analyTpmsTrack(data, offset, track);
            gpstracks.add(track);
          }
          offset += 88;
        }
      }
      data = null;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  private List<StandardDeviceTrack> filterDeviceTrack(List<StandardDeviceTrack> gpstracks, int meter, int interval, int limit, int parkTime, int temperature, int number)
  {
    if (gpstracks.size() > 0)
    {
      Collections.sort(gpstracks, new StandardTrackCompare());
      
      StandardDeviceTrack track = (StandardDeviceTrack)gpstracks.get(gpstracks.size() / 2);
      
      List<StandardDeviceTrack> lstTracks = new ArrayList();
      
      StandardDeviceTrack firstPark = null;
      StandardDeviceTrack lastPark = null;
      
      StandardDeviceTrack lastTrack = (StandardDeviceTrack)gpstracks.get(0);
      if (lastTrack.getIsParking().booleanValue())
      {
        lastTrack.setTrackTime(lastTrack.getTrackTime() - lastTrack.getParkTime().intValue() * 1000);
        lastTrack.setGpsTime(DateUtil.dateSwitchString(new Date(lastTrack.getTrackTime())));
        lastTrack.setGpsTimeStr(DateUtil.dateSwitchString(new Date(lastTrack.getTrackTime())));
        firstPark = lastTrack;
      }
      else
      {
        lstTracks.add(lastTrack);
      }
      for (int i = 1; i < gpstracks.size(); i++)
      {
        track = (StandardDeviceTrack)gpstracks.get(i);
        if ((lastTrack.getGpsTime() == track.getGpsTime()) || (lastTrack.getGpsTime().equals(track.getGpsTime()))) {
          if ((lastTrack.getStatus1().intValue() & 0x1) <= 0)
          {
            lstTracks.remove(lastTrack);
          }
          else
          {
            lastTrack = track;
            continue;
          }
        }
        if (!track.getIsParking().booleanValue())
        {
          if (firstPark != null)
          {
            if (lastPark == null) {
              lastPark = firstPark;
            }
            if (lastPark.getParkTime().intValue() > parkTime)
            {
              firstPark.setParkTime(lastPark.getParkTime());
              boolean pack = false;
              int time = (int)(track.getTrackTime() - lastTrack.getTrackTime());
              if ((interval == 0) || (time >= interval)) {
                pack = true;
              }
              int speed = firstPark.getSpeed().intValue();
              int temp1 = track.getTempSensor1().intValue();
              int temp2 = track.getTempSensor2().intValue();
              int temp3 = track.getTempSensor3().intValue();
              int temp4 = track.getTempSensor4().intValue();
              if (pack)
              {
                boolean temper = false;
                if ((limit == 0) || (speed >= limit * 10)) {
                  temper = true;
                }
                if (temper) {
                  if (number == 1)
                  {
                    if ((temperature == 0) || (temp1 >= temperature * 100)) {
                      lstTracks.add(track);
                    }
                  }
                  else if (number == 2)
                  {
                    if ((temperature == 0) || (temp1 >= temperature * 100) || (temp2 >= temperature * 100)) {
                      lstTracks.add(track);
                    }
                  }
                  else if (number == 3)
                  {
                    if ((temperature == 0) || (temp1 >= temperature * 100) || (temp2 >= temperature * 100) || (temp3 >= temperature * 100)) {
                      lstTracks.add(track);
                    }
                  }
                  else if (number == 4)
                  {
                    if ((temperature == 0) || (temp1 >= temperature * 100) || (temp2 >= temperature * 100) || (temp3 >= temperature * 100) || (temp4 >= temperature * 100)) {
                      lstTracks.add(track);
                    }
                  }
                  else {
                    lstTracks.add(track);
                  }
                }
              }
              lastTrack = track;
            }
            else if (lstTracks.size() == 0)
            {
              firstPark.setParkTime(lastPark.getParkTime());
              lstTracks.add(firstPark);
            }
            firstPark = null;
            lastPark = null;
          }
          boolean flag = false;
          boolean mark = false;
          boolean temper = false;
          int temp = track.getLiCheng().intValue() - lastTrack.getLiCheng().intValue();
          if (((meter == 0) || (temp >= meter) || (temp < 0)) && (
            (meter == 0) || (temp >= meter))) {
            flag = true;
          }
          int time = (int)(track.getTrackTime() - lastTrack.getTrackTime());
          if ((flag) && (
            (interval == 0) || (time >= interval))) {
            mark = true;
          }
          int speed = track.getSpeed().intValue();
          if ((mark) && (
            (limit == 0) || (speed >= limit * 10))) {
            temper = true;
          }
          int temp1 = track.getTempSensor1().intValue();
          int temp2 = track.getTempSensor2().intValue();
          int temp3 = track.getTempSensor3().intValue();
          int temp4 = track.getTempSensor4().intValue();
          if (temper)
          {
            lastTrack = track;
            if (number == 1)
            {
              if ((temperature == 0) || (temp1 >= temperature * 100)) {
                lstTracks.add(track);
              }
            }
            else if (number == 2)
            {
              if ((temperature == 0) || (temp1 >= temperature * 100) || (temp2 >= temperature * 100)) {
                lstTracks.add(track);
              }
            }
            else if (number == 3)
            {
              if ((temperature == 0) || (temp1 >= temperature * 100) || (temp2 >= temperature * 100) || (temp3 >= temperature * 100)) {
                lstTracks.add(track);
              }
            }
            else if (number == 4)
            {
              if ((temperature == 0) || (temp1 >= temperature * 100) || (temp2 >= temperature * 100) || (temp3 >= temperature * 100) || (temp4 >= temperature * 100)) {
                lstTracks.add(track);
              }
            }
            else {
              lstTracks.add(track);
            }
          }
        }
        else
        {
          if (firstPark == null) {
            firstPark = track;
          }
          lastPark = track;
        }
      }
      if ((firstPark != null) && (lastPark != null))
      {
        int time = (int)(lastPark.getTrackTime() - firstPark.getTrackTime());
        if ((interval == 0) || (time >= interval))
        {
          firstPark.setParkTime(lastPark.getParkTime());
          lstTracks.add(firstPark);
        }
      }
      if ((lstTracks.size() > 0) && (((StandardDeviceTrack)lstTracks.get(0)).getSpeed().intValue() < limit * 10)) {
        lstTracks.remove(0);
      }
      if ((lstTracks.size() > 0) && (temperature != 0)) {
        if (number == 1)
        {
          if (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor1().intValue() < temperature * 100) {
            lstTracks.remove(0);
          }
        }
        else if (number == 2)
        {
          if ((((StandardDeviceTrack)lstTracks.get(0)).getTempSensor1().intValue() < temperature * 100) && (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor2().intValue() < temperature * 100)) {
            lstTracks.remove(0);
          }
        }
        else if (number == 3)
        {
          if ((((StandardDeviceTrack)lstTracks.get(0)).getTempSensor1().intValue() < temperature * 100) && (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor2().intValue() < temperature * 100) && 
            (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor3().intValue() < temperature * 100)) {
            lstTracks.remove(0);
          }
        }
        else if ((number == 4) && 
          (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor1().intValue() < temperature * 100) && (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor2().intValue() < temperature * 100) && 
          (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor3().intValue() < temperature * 100) && (((StandardDeviceTrack)lstTracks.get(0)).getTempSensor4().intValue() < temperature * 100)) {
          lstTracks.remove(0);
        }
      }
      return lstTracks;
    }
    gpstracks = null;
    return null;
  }
  
  private String getQueryTpmsString(String dateB, String dateE, String vehiIdno, String devIdno)
  {
    StringBuffer strQuery = new StringBuffer("SELECT b.VehiIDNO as vehiIdno, b.plateType as plateType, a.DevIDNO as devIdno, a.GPSDate as gpsDate, a.Type as type, a.TransportData as transportData FROM jt808_vehicle_transport a,jt808_vehicle_info b where Type = 101 ");
    
    strQuery.append(String.format("and GPSDate >= '%s' and GPSDate <= '%s' ", new Object[] {
      dateB, dateE }));
    strQuery.append(" and a.VehiID = b.ID ");
    if (devIdno != null) {
      strQuery.append(String.format("and devIdno = '%s' ", new Object[] { devIdno }));
    } else {
      strQuery.append(String.format("and b.vehiIdno = '%s' ", new Object[] { vehiIdno }));
    }
    return strQuery.toString();
  }
  
  private String getQueryString(String dateB, String dateE, String vehiIdno, String devIdno)
  {
    StringBuffer strQuery = new StringBuffer("SELECT b.VehiIDNO as vehiIdno, b.plateType as plateType, a.devIDNO as devIdno, a.gpsDate as gpsDate, a.gpsData as gpsData FROM jt808_vehicle_gps a,jt808_vehicle_info b ");
    
    strQuery.append(String.format("where gpsDate >= '%s' and gpsDate <= '%s' ", new Object[] {
      dateB, dateE }));
    strQuery.append(" and a.VehiID = b.ID ");
    if (devIdno != null) {
      strQuery.append(String.format("and devIdno = '%s' ", new Object[] { devIdno }));
    } else {
      strQuery.append(String.format("and b.vehiIdno = '%s' ", new Object[] { vehiIdno }));
    }
    return strQuery.toString();
  }
  
  public static List<QueryScalar> getStandardDeviceGpsQueryScalar()
  {
    List<QueryScalar> scalars = new ArrayList();
    scalars.add(new QueryScalar("vehiIdno", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("plateType", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("devIdno", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("gpsDate", StandardBasicTypes.TIMESTAMP));
    scalars.add(new QueryScalar("gpsData", StandardBasicTypes.BLOB));
    return scalars;
  }
  
  public static List<QueryScalar> getStandardTpmsGpsQueryScalar()
  {
    List<QueryScalar> scalars = new ArrayList();
    scalars.add(new QueryScalar("vehiIdno", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("plateType", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("type", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("devIdno", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("gpsDate", StandardBasicTypes.TIMESTAMP));
    scalars.add(new QueryScalar("transportData", StandardBasicTypes.BLOB));
    return scalars;
  }
  
  public AjaxDto<StandardDeviceTrack> queryDeviceGps(String vehiIdno, Date begin, Date end, int meter, int interval, int limit, int parkTime, int temperature, int number, Pagination pagination, String toMap, String devIdno)
    throws Exception
  {
    List<StandardDeviceTrack> gpstracks = new ArrayList();
    
    AjaxDto<StandardDeviceGps> ajaxDto = this.paginationDao.getExtraByNativeSqlEx(getQueryString(DateUtil.dateSwitchDateString(begin), 
      DateUtil.dateSwitchDateString(end), vehiIdno, devIdno), null, getStandardDeviceGpsQueryScalar(), StandardDeviceGps.class, null);
    if ((ajaxDto.getPageList() != null) && (ajaxDto.getPageList().size() > 0)) {
      for (int i = 0; i < ajaxDto.getPageList().size(); i++)
      {
        StandardDeviceGps gps = (StandardDeviceGps)ajaxDto.getPageList().get(i);
        if (gps.getGpsData() != null) {
          analyDeviceGps(gps, vehiIdno, begin.getTime(), end.getTime(), gpstracks, toMap);
        }
      }
    }
    AjaxDto<StandardDeviceTrack> ajaxTrack = new AjaxDto();
    
    List<StandardDeviceTrack> tracks = filterDeviceTrack(gpstracks, meter, interval, limit, parkTime, temperature, number);
    
    int totalRecord = 0;
    if (tracks != null)
    {
      totalRecord = tracks.size();
      if (pagination != null)
      {
        if (tracks.size() < (pagination.getCurrentPage() - 1) * pagination.getPageRecords()) {
          pagination.setCurrentPage(1);
        }
        int offset = (pagination.getCurrentPage() - 1) * pagination.getPageRecords();
        int endOffset = pagination.getCurrentPage() * pagination.getPageRecords();
        List<StandardDeviceTrack> retTracks = new ArrayList();
        for (int i = offset; (i < endOffset) && (i < tracks.size()); i++) {
          retTracks.add((StandardDeviceTrack)tracks.get(i));
        }
        ajaxTrack.setPageList(retTracks);
      }
      else
      {
        ajaxTrack.setPageList(tracks);
      }
    }
    else if (pagination != null)
    {
      pagination.setCurrentPage(1);
    }
    if (pagination != null)
    {
      Pagination pagin = new Pagination(pagination.getPageRecords(), pagination.getCurrentPage(), totalRecord, pagination.getSortParams());
      ajaxTrack.setPagination(pagin);
    }
    return ajaxTrack;
  }
  
  public AjaxDto<StandardTpmsTrack> queryTpmsGps(String vehiIdno, Date begin, Date end, Pagination pagination, String toMap, String devIdno)
    throws Exception
  {
    List<StandardTpmsTrack> gpstracks = new ArrayList();
    AjaxDto<StandardTransportGps> ajaxDto = this.paginationDao.getExtraByNativeSqlEx(getQueryTpmsString(DateUtil.dateSwitchDateString(begin), 
      DateUtil.dateSwitchDateString(end), vehiIdno, devIdno), null, getStandardTpmsGpsQueryScalar(), StandardTransportGps.class, null);
    if ((ajaxDto.getPageList() != null) && (ajaxDto.getPageList().size() > 0)) {
      for (int i = 0; i < ajaxDto.getPageList().size(); i++)
      {
        StandardTransportGps gps = (StandardTransportGps)ajaxDto.getPageList().get(i);
        if (gps.getTransportData() != null) {
          analyTpmsGps(gps, vehiIdno, begin.getTime(), end.getTime(), gpstracks, toMap);
        }
      }
    }
    AjaxDto<StandardTpmsTrack> ajaxTrack = new AjaxDto();
    if (gpstracks.size() > 0) {
      Collections.sort(gpstracks, new StandardTpmsTrackCompare());
    }
    int totalRecord = 0;
    if (gpstracks != null)
    {
      totalRecord = gpstracks.size();
      if (pagination != null)
      {
        if (gpstracks.size() < (pagination.getCurrentPage() - 1) * pagination.getPageRecords()) {
          pagination.setCurrentPage(1);
        }
        int offset = (pagination.getCurrentPage() - 1) * pagination.getPageRecords();
        int endOffset = pagination.getCurrentPage() * pagination.getPageRecords();
        List<StandardTpmsTrack> retTracks = new ArrayList();
        for (int i = offset; (i < endOffset) && (i < gpstracks.size()); i++) {
          retTracks.add((StandardTpmsTrack)gpstracks.get(i));
        }
        ajaxTrack.setPageList(retTracks);
      }
      else
      {
        ajaxTrack.setPageList(gpstracks);
      }
    }
    else if (pagination != null)
    {
      pagination.setCurrentPage(1);
    }
    if (pagination != null)
    {
      Pagination pagin = new Pagination(pagination.getPageRecords(), pagination.getCurrentPage(), totalRecord, pagination.getSortParams());
      ajaxTrack.setPagination(pagin);
    }
    return ajaxTrack;
  }
  
  protected void appendDeviceCondition(StringBuffer strQuery, String[] devIDNO)
  {
    strQuery.append(String.format(" and (devIDNO = '%s' ", new Object[] { devIDNO[0] }));
    for (int i = 1; i < devIDNO.length; i++) {
      strQuery.append(String.format("or devIDNO = '%s' ", new Object[] { devIDNO[i] }));
    }
    strQuery.append(") ");
  }
  
  protected String getTableColumn(String qtype)
  {
    String column = "";
    if ((qtype != null) && (!qtype.isEmpty())) {
      if ("devIdno".equals(qtype)) {
        column = "DevIdno";
      } else {
        column = qtype;
      }
    }
    return column;
  }
  
  private List<QueryScalar> getStandardDeviceMinMaxGpsQueryScalar()
  {
    List<QueryScalar> scalars = new ArrayList();
    scalars.add(new QueryScalar("vehiIdno", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("plateType", StandardBasicTypes.INTEGER));
    scalars.add(new QueryScalar("devIdno", StandardBasicTypes.STRING));
    scalars.add(new QueryScalar("minDate", StandardBasicTypes.TIMESTAMP));
    scalars.add(new QueryScalar("minData", StandardBasicTypes.BLOB));
    scalars.add(new QueryScalar("maxDate", StandardBasicTypes.TIMESTAMP));
    scalars.add(new QueryScalar("maxData", StandardBasicTypes.BLOB));
    return scalars;
  }
  
  public AjaxDto<StandardDeviceMinMaxGps> queryGpsDate(String dateB, String dateE, String[] devIdno, Pagination pagination, String toMap)
  {
    StringBuffer strQuery = new StringBuffer("SELECT D.VehiIDNO as vehiIdno,D.PlateType as plateType,A.devIDNO as devIdno, A.MinDate as minDate, A.MinData as minData, B.MaxDate as maxDate, B.MaxData as maxData FROM ");
    strQuery.append(" (SELECT C.VehiID,C.devIDNO, C.GPSDate as MinDate,C.GPSData as MinData from jt808_vehicle_gps C,(SELECT devIDNO,MIN(GPSDate) as Min from jt808_vehicle_gps ");
    strQuery.append(String.format(" where GPSDate >= '%s' and", new Object[] { dateB.substring(0, 10) }));
    strQuery.append(String.format(" GPSDate <= '%s' ", new Object[] { dateE.substring(0, 10) }));
    appendDeviceCondition(strQuery, devIdno);
    strQuery.append(" GROUP BY devIDNO) E where C.devIDNO = E.devIDNO and C.GPSDate = E.Min) A,");
    strQuery.append(" (SELECT F.VehiID,F.devIDNO, F.GPSDate as MaxDate,F.GPSData as MaxData from jt808_vehicle_gps F,(SELECT devIDNO,MAX(GPSDate) as Max from jt808_vehicle_gps ");
    strQuery.append(String.format(" where GPSDate >= '%s' and", new Object[] { dateB.substring(0, 10) }));
    strQuery.append(String.format(" GPSDate <= '%s' ", new Object[] { dateE.substring(0, 10) }));
    appendDeviceCondition(strQuery, devIdno);
    strQuery.append(" GROUP BY devIDNO) G where G.devIDNO = F.devIDNO and G.Max = F.GPSDate) B,");
    strQuery.append("jt808_vehicle_info D ");
    strQuery.append(" WHERE A.VehiID = D.ID and A.devIDNO = B.devIDNO and B.VehiID = D.ID order by A.VehiID");
    return this.paginationDao.getExtraByNativeSqlEx(strQuery.toString(), pagination, getStandardDeviceMinMaxGpsQueryScalar(), StandardDeviceMinMaxGps.class, null);
  }
  
  public AjaxDto<StandardDeviceTrack> queryDateGps(String dateB, String dateE, boolean isMaxDate, String[] devIdno, Pagination pagination, String toMap)
  {
    StringBuffer strQuery = new StringBuffer("SELECT D.VehiIDNO as vehiIdno,A.devIDNO as devIdno, A.gpsDate as gpsDate, A.gpsData as gpsData FROM jt808_vehicle_gps A, ");
    if (isMaxDate) {
      strQuery.append(" (SELECT VehiID,devIDNO, MAX(GPSDate) as max_day from jt808_vehicle_gps");
    } else {
      strQuery.append(" (SELECT VehiID,devIDNO, MIN(GPSDate) as max_day from jt808_vehicle_gps");
    }
    strQuery.append(String.format(" where GPSDate >= '%s' and", new Object[] { dateB.substring(0, 10) }));
    strQuery.append(String.format(" GPSDate <= '%s' ", new Object[] { dateE.substring(0, 10) }));
    appendDeviceCondition(strQuery, devIdno);
    strQuery.append(" GROUP BY devIDNO) B");
    strQuery.append(",jt808_vehicle_info D ");
    strQuery.append(" WHERE A.GPSDate = B.max_day and A.VehiID = D.ID and A.devIDNO = B.devIDNO and B.VehiID = D.ID order by A.VehiID");
    
    List<StandardDeviceTrack> trackResult = new ArrayList();
    AjaxDto<StandardDeviceGps> dtoGps = this.paginationDao.getExtraByNativeSqlEx(strQuery.toString(), pagination, getStandardDeviceGpsQueryScalar(), StandardDeviceGps.class, null);
    if ((dtoGps.getPageList() != null) && (dtoGps.getPageList().size() > 0)) {
      for (int i = 0; i < dtoGps.getPageList().size(); i++)
      {
        StandardDeviceGps gps = (StandardDeviceGps)dtoGps.getPageList().get(i);
        List<StandardDeviceTrack> gpstracks = new ArrayList();
        analyDeviceGps(gps, gps.getVehiIdno(), DateUtil.StrLongTime2Date(dateB).getTime(), 
          DateUtil.StrLongTime2Date(dateE).getTime(), gpstracks, toMap);
        
        StandardDeviceTrack track = null;
        track = searchDeviceTrack(gpstracks, isMaxDate, 0);
        if (track != null) {
          trackResult.add(track);
        }
      }
    }
    Collections.sort(trackResult, new StandardTrackCompare());
    AjaxDto<StandardDeviceTrack> dtoGpsTrack = new AjaxDto();
    dtoGpsTrack.setPagination(dtoGps.getPagination());
    dtoGpsTrack.setPageList(trackResult);
    return dtoGpsTrack;
  }
  
  public StandardDeviceTrack searchDeviceTrack(List<StandardDeviceTrack> gpstracks, boolean isMaxDate, int start)
  {
    StandardDeviceTrack track = new StandardDeviceTrack();
    String vehiIdno = "";
    String gpsTime = "";
    if (gpstracks.size() > 0) {
      vehiIdno = ((StandardDeviceTrack)gpstracks.get(0)).getVehiIdno();
    } else {
      return null;
    }
    StandardDeviceTrack track2 = new StandardDeviceTrack();
    if (isMaxDate) {
      track2 = (StandardDeviceTrack)gpstracks.get(gpstracks.size() - 1);
    } else {
      track2 = (StandardDeviceTrack)gpstracks.get(0);
    }
    while (gpstracks.size() > 0)
    {
      if (isMaxDate)
      {
        track = (StandardDeviceTrack)gpstracks.get(gpstracks.size() - 1);
        if ((track.getLiCheng() != null) && (track.getJingDu() != null) && (track.getWeiDu() != null) && 
          (track.getLiCheng().intValue() != 0) && (track.getJingDu().intValue() != 0) && (track.getWeiDu().intValue() != 0)) {
          return track;
        }
        gpstracks.remove(gpstracks.size() - 1);
      }
      else
      {
        track = (StandardDeviceTrack)gpstracks.get(0);
        if ((track.getLiCheng() != null) && (track.getJingDu() != null) && (track.getWeiDu() != null) && 
          (track.getLiCheng().intValue() != 0) && (track.getJingDu().intValue() != 0) && (track.getWeiDu().intValue() != 0)) {
          return track;
        }
        gpstracks.remove(0);
      }
      if (start == 1) {
        return track;
      }
    }
    return track2;
  }
  
  public List<StandardDeviceGps> queryDeviceTrack(String dateB, String dateE, boolean isMaxDate, String vehiIdno, String toMap)
  {
    StringBuffer strQuery = new StringBuffer("SELECT b.VehiIDNO as vehiIdno,a.devIDNO as devIdno, a.gpsDate as gpsDate, a.gpsData as gpsData FROM jt808_vehicle_gps a, jt808_vehicle_info b");
    strQuery.append(" WHERE a.VehiID = b.ID and");
    strQuery.append(String.format(" GPSDate >= '%s' and", new Object[] { dateB.substring(0, 10) }));
    strQuery.append(String.format(" GPSDate <= '%s' ", new Object[] { dateE.substring(0, 10) }));
    strQuery.append(String.format(" and  VehiIDNO = '%s' ", new Object[] { vehiIdno }));
    return this.paginationDao.getExtraByNativeSqlEx(strQuery.toString(), null, getStandardDeviceGpsQueryScalar(), StandardDeviceGps.class, null).getPageList();
  }
  
  public List<StandardDeviceTrack> resolveDeviceTrack(StandardDeviceGps gps, String dateB, String dateE, String toMap)
  {
    List<StandardDeviceTrack> gpstracks = new ArrayList();
    analyDeviceGps(gps, gps.getVehiIdno(), DateUtil.StrLongTime2Date(dateB).getTime(), 
      DateUtil.StrLongTime2Date(dateE).getTime(), gpstracks, toMap);
    return gpstracks;
  }
  
  public AjaxDto<StandardDeviceAlarmSummary> queryLineDeviceAlarmSummary(String begin, String end, String[] vids, String alarmTime, String speed, String rate, String dids, String toMap, Pagination pagination)
  {
    List<StandardDeviceTrack> gpstracks = new ArrayList();
    AjaxDto<StandardDeviceGps> ajaxDto = this.paginationDao.getExtraByNativeSqlEx(getLineQueryString(begin, 
      end, vids, dids), null, getStandardDeviceGpsQueryScalar(), StandardDeviceGps.class, null);
    if ((ajaxDto.getPageList() != null) && (ajaxDto.getPageList().size() > 0)) {
      for (int i = 0; i < ajaxDto.getPageList().size(); i++)
      {
        StandardDeviceGps gps = (StandardDeviceGps)ajaxDto.getPageList().get(i);
        if (gps.getGpsData() != null) {
          analyDeviceGps(gps, gps.getVehiIdno(), DateUtil.StrLongTime2Date(begin).getTime(), DateUtil.StrLongTime2Date(end).getTime(), gpstracks, toMap);
        }
      }
    }
    AjaxDto<StandardDeviceAlarmSummary> ajaxTrack = new AjaxDto();
    List<StandardDeviceAlarmSummary> alarms = filterLineDeviceAlarmSummary(vids, gpstracks, alarmTime, speed, rate);
    int totalRecord = 0;
    if (alarms != null)
    {
      totalRecord = alarms.size();
      if (pagination != null)
      {
        if (alarms.size() < (pagination.getCurrentPage() - 1) * pagination.getPageRecords()) {
          pagination.setCurrentPage(1);
        }
        int offset = (pagination.getCurrentPage() - 1) * pagination.getPageRecords();
        int endOffset = pagination.getCurrentPage() * pagination.getPageRecords();
        List<StandardDeviceAlarmSummary> retTracks = new ArrayList();
        for (int i = offset; (i < endOffset) && (i < alarms.size()); i++) {
          retTracks.add((StandardDeviceAlarmSummary)alarms.get(i));
        }
        ajaxTrack.setPageList(retTracks);
      }
      else
      {
        ajaxTrack.setPageList(alarms);
      }
    }
    else if (pagination != null)
    {
      pagination.setCurrentPage(1);
    }
    if (pagination != null)
    {
      Pagination pagin = new Pagination(pagination.getPageRecords(), pagination.getCurrentPage(), totalRecord, pagination.getSortParams());
      ajaxTrack.setPagination(pagin);
    }
    return ajaxTrack;
  }
  
  private String getLineQueryString(String dateB, String dateE, String[] vehiIdnos, String dids)
  {
    StringBuffer strQuery = new StringBuffer("SELECT b.VehiIDNO as vehiIdno, b.plateType as plateType, a.devIDNO as devIdno, a.gpsDate as gpsDate, a.gpsData as gpsData FROM jt808_vehicle_gps a,jt808_vehicle_info b ");
    strQuery.append(String.format("where gpsDate >= '%s' and gpsDate <= '%s' ", new Object[] {
      dateB, dateE }));
    strQuery.append(" and a.VehiID = b.ID ");
    String[] devIdnos = dids.split(",");
    if (devIdnos.length > 0)
    {
      strQuery.append(String.format("and devIdno in ( '%s' ", new Object[] { devIdnos[0] }));
      for (int i = 1; i < devIdnos.length; i++) {
        strQuery.append(String.format(", '%s' ", new Object[] { devIdnos[i] }));
      }
      strQuery.append(" )");
    }
    return strQuery.toString();
  }
  
  private List<StandardDeviceAlarmSummary> filterLineDeviceAlarmSummary(String[] vids, List<StandardDeviceTrack> gpstracks, String alarmTime, String speed, String rate)
  {
    if (gpstracks.size() > 0)
    {
      Collections.sort(gpstracks, new StandardTrackCompare());
      
      StandardDeviceTrack track = null;
      
      List<StandardDeviceAlarmSummary> lstAlarms = new ArrayList();
      Map<String, StandardDeviceAlarmSummary> vehiAndAlarm = new HashMap();
      StandardDeviceAlarmEx alarm = new StandardDeviceAlarmEx();
      double sp = 0.0D;
      double rt = 0.0D;
      int time = 0;
      if ((speed != null) && (!speed.isEmpty())) {
        sp = Double.parseDouble(speed);
      }
      if ((rate != null) && (!rate.isEmpty())) {
        rt = Double.parseDouble(rate);
      }
      if ((alarmTime != null) && (!alarmTime.isEmpty())) {
        time = Integer.parseInt(alarmTime);
      }
      for (int i = 0; i < vids.length; i++)
      {
        StandardDeviceAlarmSummary devAlarm = null;
        for (int j = 0; j < gpstracks.size(); j++)
        {
          track = (StandardDeviceTrack)gpstracks.get(j);
          if (track.getVehiIdno().equals(vids[i])) {
            if ((track.getSpeed() != null) && (track.getSpeed().intValue() >= sp * (10.0D + rt / 10.0D)))
            {
              if (alarm.getStm() == null) {
                alarm.setStm(DateUtil.StrLongTime2Date(track.getGpsTime()));
              }
            }
            else if (alarm.getStm() != null) {
              if (DateUtil.StrLongTime2Date(track.getGpsTime()).getTime() - alarm.getStm().getTime() > time * 1000)
              {
                if (devAlarm == null)
                {
                  devAlarm = new StandardDeviceAlarmSummary();
                  devAlarm.setVehiIdno(vids[i]);
                  devAlarm.setCompanyId(track.getLineID());
                  devAlarm.setPlateType(track.getPlateType());
                  devAlarm.setCount(Integer.valueOf(1));
                  devAlarm.setBeginTime(alarm.getStm());
                  devAlarm.setEndTime(DateUtil.StrLongTime2Date(track.getGpsTime()));
                }
                else
                {
                  devAlarm.setCount(Integer.valueOf(devAlarm.getCount().intValue() + 1));
                  if ((devAlarm.getBeginTime() == null) || (DateUtil.compareDate(devAlarm.getBeginTime(), alarm.getStm()))) {
                    devAlarm.setBeginTime(alarm.getStm());
                  }
                  if ((devAlarm.getEndTime() == null) || (DateUtil.compareDate(DateUtil.StrLongTime2Date(track.getGpsTime()), devAlarm.getEndTime()))) {
                    devAlarm.setEndTime(DateUtil.StrLongTime2Date(track.getGpsTime()));
                  }
                }
                alarm.setStm(null);
                vehiAndAlarm.put(vids[i], devAlarm);
              }
              else
              {
                alarm.setStm(null);
              }
            }
          }
        }
      }
      for (Iterator<Map.Entry<String, StandardDeviceAlarmSummary>> it = vehiAndAlarm.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry<String, StandardDeviceAlarmSummary> entry = (Map.Entry)it.next();
        for (int i = 0; i < vids.length; i++) {
          if (((StandardDeviceAlarmSummary)entry.getValue()).getVehiIdno().toString().equals(vids[i]))
          {
            StandardDeviceAlarmSummary summary = (StandardDeviceAlarmSummary)entry.getValue();
            lstAlarms.add(summary);
            break;
          }
        }
      }
      return lstAlarms;
    }
    gpstracks = null;
    return null;
  }
  
  public AjaxDto<StandardDeviceAlarmEx> queryLineDeviceAlarmDetail(String begin, String end, String[] vids, String alarmTime, String speed, String rate, String dids, String toMap, Pagination pagination)
  {
    List<StandardDeviceTrack> gpstracks = new ArrayList();
    AjaxDto<StandardDeviceGps> ajaxDto = this.paginationDao.getExtraByNativeSqlEx(getLineQueryString(begin, 
      end, vids, dids), null, getStandardDeviceGpsQueryScalar(), StandardDeviceGps.class, null);
    if ((ajaxDto.getPageList() != null) && (ajaxDto.getPageList().size() > 0)) {
      for (int i = 0; i < ajaxDto.getPageList().size(); i++)
      {
        StandardDeviceGps gps = (StandardDeviceGps)ajaxDto.getPageList().get(i);
        if (gps.getGpsData() != null) {
          analyDeviceGps(gps, gps.getVehiIdno(), DateUtil.StrLongTime2Date(begin).getTime(), DateUtil.StrLongTime2Date(end).getTime(), gpstracks, toMap);
        }
      }
    }
    AjaxDto<StandardDeviceAlarmEx> ajaxTrack = new AjaxDto();
    List<StandardDeviceAlarmEx> alarms = filterLineDeviceAlarm(vids, gpstracks, alarmTime, speed, rate);
    int totalRecord = 0;
    if (alarms != null)
    {
      totalRecord = alarms.size();
      if (pagination != null)
      {
        if (alarms.size() < (pagination.getCurrentPage() - 1) * pagination.getPageRecords()) {
          pagination.setCurrentPage(1);
        }
        int offset = (pagination.getCurrentPage() - 1) * pagination.getPageRecords();
        int endOffset = pagination.getCurrentPage() * pagination.getPageRecords();
        List<StandardDeviceAlarmEx> retTracks = new ArrayList();
        for (int i = offset; (i < endOffset) && (i < alarms.size()); i++) {
          retTracks.add((StandardDeviceAlarmEx)alarms.get(i));
        }
        ajaxTrack.setPageList(retTracks);
      }
      else
      {
        ajaxTrack.setPageList(alarms);
      }
    }
    else if (pagination != null)
    {
      pagination.setCurrentPage(1);
    }
    if (pagination != null)
    {
      Pagination pagin = new Pagination(pagination.getPageRecords(), pagination.getCurrentPage(), totalRecord, pagination.getSortParams());
      ajaxTrack.setPagination(pagin);
    }
    return ajaxTrack;
  }
  
  private List<StandardDeviceAlarmEx> filterLineDeviceAlarm(String[] vids, List<StandardDeviceTrack> gpstracks, String alarmTime, String speed, String rate)
  {
    if (gpstracks.size() > 0)
    {
      Collections.sort(gpstracks, new StandardTrackCompare());
      
      StandardDeviceTrack track = null;
      
      List<StandardDeviceAlarmEx> lstAlarms = new ArrayList();
      StandardDeviceAlarmEx alarm = new StandardDeviceAlarmEx();
      double sp = 0.0D;
      double rt = 0.0D;
      int time = 0;
      if ((speed != null) && (!speed.isEmpty())) {
        sp = Double.parseDouble(speed);
      }
      if ((rate != null) && (!rate.isEmpty())) {
        rt = Double.parseDouble(rate);
      }
      if ((alarmTime != null) && (!alarmTime.isEmpty())) {
        time = Integer.parseInt(alarmTime);
      }
      for (int i = 0; i < vids.length; i++) {
        for (int j = 0; j < gpstracks.size(); j++)
        {
          track = (StandardDeviceTrack)gpstracks.get(j);
          if (track.getVehiIdno().equals(vids[i])) {
            if ((track.getSpeed() != null) && (track.getSpeed().intValue() >= sp * (10.0D + rt / 10.0D)))
            {
              if (alarm.getStm() == null)
              {
                alarm.setVid(vids[i]);
                alarm.setStm(DateUtil.StrLongTime2Date(track.getGpsTime()));
                alarm.setSlc(track.getLiCheng());
                alarm.setSlng(track.getJingDu());
                alarm.setSlat(track.getWeiDu());
                alarm.setSsp(track.getSpeed());
                alarm.setP1(track.getLineID());
                alarm.setP2(track.getDirection());
                alarm.setP3(track.getPlateType());
              }
            }
            else if ((alarm.getStm() != null) && (DateUtil.StrLongTime2Date(track.getGpsTime()).getTime() - alarm.getStm().getTime() > time * 1000))
            {
              alarm.setEtm(DateUtil.StrLongTime2Date(track.getGpsTime()));
              alarm.setElc(track.getLiCheng());
              alarm.setElng(track.getJingDu());
              alarm.setElat(track.getWeiDu());
              alarm.setEsp(track.getSpeed());
              lstAlarms.add(alarm);
              alarm = new StandardDeviceAlarmEx();
            }
            else
            {
              alarm = new StandardDeviceAlarmEx();
            }
          }
        }
      }
      Collections.sort(lstAlarms, new DeviceAlarmCompare());
      return lstAlarms;
    }
    gpstracks = null;
    return null;
  }
}
