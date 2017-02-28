package com.gps808.report.service;

import com.framework.web.dto.AjaxDto;
import com.framework.web.dto.Pagination;
import com.framework.web.service.UniversalService;
import com.gps808.model.StandardDeviceGps;
import com.gps808.report.vo.StandardDeviceAlarmEx;
import com.gps808.report.vo.StandardDeviceAlarmSummary;
import com.gps808.report.vo.StandardDeviceMinMaxGps;
import com.gps808.report.vo.StandardDeviceTrack;
import com.gps808.report.vo.StandardTpmsTrack;
import java.util.Date;
import java.util.List;

public abstract interface StandardVehicleGpsService
  extends UniversalService
{
  public abstract AjaxDto<StandardDeviceTrack> queryDeviceGps(String paramString1, Date paramDate1, Date paramDate2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6, Pagination paramPagination, String paramString2, String paramString3)
    throws Exception;
  
  public abstract AjaxDto<StandardTpmsTrack> queryTpmsGps(String paramString1, Date paramDate1, Date paramDate2, Pagination paramPagination, String paramString2, String paramString3)
    throws Exception;
  
  public abstract AjaxDto<StandardDeviceMinMaxGps> queryGpsDate(String paramString1, String paramString2, String[] paramArrayOfString, Pagination paramPagination, String paramString3);
  
  public abstract AjaxDto<StandardDeviceTrack> queryDateGps(String paramString1, String paramString2, boolean paramBoolean, String[] paramArrayOfString, Pagination paramPagination, String paramString3);
  
  public abstract List<StandardDeviceGps> queryDeviceTrack(String paramString1, String paramString2, boolean paramBoolean, String paramString3, String paramString4);
  
  public abstract void analyDeviceGps(StandardDeviceGps paramStandardDeviceGps, String paramString1, long paramLong1, long paramLong2, List<StandardDeviceTrack> paramList, String paramString2);
  
  public abstract StandardDeviceTrack searchDeviceTrack(List<StandardDeviceTrack> paramList, boolean paramBoolean, int paramInt);
  
  public abstract List<StandardDeviceTrack> resolveDeviceTrack(StandardDeviceGps paramStandardDeviceGps, String paramString1, String paramString2, String paramString3);
  
  public abstract AjaxDto<StandardDeviceAlarmSummary> queryLineDeviceAlarmSummary(String paramString1, String paramString2, String[] paramArrayOfString, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, Pagination paramPagination);
  
  public abstract AjaxDto<StandardDeviceAlarmEx> queryLineDeviceAlarmDetail(String paramString1, String paramString2, String[] paramArrayOfString, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, Pagination paramPagination);
}
