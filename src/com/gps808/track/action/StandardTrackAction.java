package com.gps808.track.action;

import com.framework.logger.Logger;
import com.framework.utils.DateUtil;
import com.framework.utils.ExportReport;
import com.framework.web.dto.AjaxDto;
import com.framework.web.dto.Pagination;
import com.gps.common.service.DeviceService;
import com.gps.model.DeviceStatusLite;
import com.gps808.model.StandardUserRole;
import com.gps808.report.action.base.StandardReportBaseAction;
import com.gps808.report.service.StandardVehicleGpsService;
import com.gps808.report.vo.StandardDeviceTrack;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class StandardTrackAction
  extends StandardReportBaseAction
{
  private static final long serialVersionUID = -9186083421023326883L;
  protected DeviceService deviceService;
  
  public DeviceService getDeviceService()
  {
    return this.deviceService;
  }
  
  public void setDeviceService(DeviceService deviceService)
  {
    this.deviceService = deviceService;
  }
  
  protected boolean checkPrivi()
  {
    return isRole(StandardUserRole.PRIVI_PAGE_TRACK.toString());
  }
  
  protected void queryGpsTrack(String distance, String parkTime, Pagination pagination, Integer type, boolean nameEncoder)
    throws Exception
  {
    try
    {
      String begintime = getRequestString("begintime");
      String endtime = getRequestString("endtime");
      String vehiIdno = "";
      if (!nameEncoder) {
        vehiIdno = getRequest().getParameter("vehiIdno");
      } else {
        vehiIdno = getRequestString("vehiIdno");
      }
      String toMap = getRequestString("toMap");
      if ((begintime == null) || (endtime == null) || (vehiIdno == null) || 
        (!DateUtil.isLongTimeValid(begintime)) || (!DateUtil.isLongTimeValid(endtime)))
      {
        addCustomResponse(ACTION_RESULT, Integer.valueOf(8));
      }
      else
      {
        int meter = 0;
        if ((distance != null) && (!distance.isEmpty())) {
          meter = (int)(Double.parseDouble(distance) * 1000.0D);
        }
        int park = 0;
        if ((parkTime != null) && (!parkTime.isEmpty())) {
          park = Integer.parseInt(parkTime);
        }
        String devIdno = null;
        if ((type != null) && (type.intValue() == 1)) {
          devIdno = getOilDevIdno(vehiIdno);
        } else {
          devIdno = getGPSDevIdno(vehiIdno);
        }
        AjaxDto<StandardDeviceTrack> ajaxDto = this.vehicleGpsService.queryDeviceGps(vehiIdno, DateUtil.StrLongTime2Date(begintime), 
          DateUtil.StrLongTime2Date(endtime), meter, 0, 0, park, 0, 0, pagination, toMap, devIdno);
        
        List<StandardDeviceTrack> tracks = ajaxDto.getPageList();
        List<DeviceStatusLite> tracklites = null;
        if (tracks != null)
        {
          String[] devIdnos = new String[1];
          devIdnos[0] = devIdno;
          AjaxDto<DeviceStatusLite> dtoAjax = this.deviceService.getDeviceStatusLite(devIdnos);
          DeviceStatusLite status = null;
          if ((dtoAjax.getPageList() != null) && (dtoAjax.getPageList().size() >= 1)) {
            status = (DeviceStatusLite)dtoAjax.getPageList().get(0);
          }
          tracklites = new ArrayList();
          for (int i = 0; i < tracks.size(); i++)
          {
            DeviceStatusLite lite = new DeviceStatusLite();
            lite.setStatusLite((StandardDeviceTrack)tracks.get(i));
            if (status != null)
            {
              lite.setPt(status.getPt());
              lite.setDt(status.getDt());
              lite.setAc(status.getAc());
              lite.setFt(status.getFt());
              lite.setFdt(status.getFdt());
            }
            tracklites.add(lite);
          }
        }
        addCustomResponse("infos", tracklites);
        addCustomResponse("pagination", ajaxDto.getPagination());
      }
    }
    catch (Exception ex)
    {
      this.log.error(ex.getMessage(), ex);
      addCustomResponse(ACTION_RESULT, Integer.valueOf(1));
    }
  }
  
  public String query()
    throws Exception
  {
    String distance = getRequestString("distance");
    String parkTime = getRequestString("parkTime");
    if ((distance == null) || (distance.isEmpty()) || 
      (parkTime == null) || (parkTime.isEmpty())) {
      addCustomResponse(ACTION_RESULT, Integer.valueOf(8));
    } else {
      queryGpsTrack(distance, parkTime, getRequestPagination(), null, false);
    }
    return "success";
  }
  
  public String queryEx()
    throws Exception
  {
    String distance = getRequestString("distance");
    String parkTime = getRequestString("parkTime");
    if ((distance == null) || (distance.isEmpty()) || 
      (parkTime == null) || (parkTime.isEmpty())) {
      addCustomResponse(ACTION_RESULT, Integer.valueOf(8));
    } else {
      queryGpsTrack(distance, parkTime, getRequestPagination(), null, true);
    }
    return "success";
  }
  
  protected String[] genGpstrackHeads()
  {
    String[] heads = new String[6];
    heads[0] = getText("report.index");
    heads[1] = getText("report.vehicle");
    heads[2] = getText("report.time");
    heads[3] = getText("report.currentPosition");
    heads[4] = (getText("report.currentSpeed") + getSpeedUnit());
    heads[5] = (getText("report.currentLiCheng") + getLiChengUnit());
    return heads;
  }
  
  protected void genGpstrackData(String begintime, String endtime, Integer toMap, String vehiIdno, ExportReport export)
  {
    try
    {
      String distance = getRequestString("distance");
      String time = getRequestString("time");
      String speed = getRequestString("speed");
      String parkTime = getRequestString("parktime");
      int meter = 0;
      if ((distance != null) && (!distance.isEmpty())) {
        meter = (int)(Double.parseDouble(distance) * 1000.0D);
      }
      int park = 0;
      if ((parkTime != null) && (!parkTime.isEmpty())) {
        park = Integer.parseInt(parkTime);
      }
      int interval = 0;
      if ((time != null) && (!time.isEmpty())) {
        interval = Integer.parseInt(time) * 1000;
      }
      int limit = 0;
      if ((speed != null) && (!speed.isEmpty())) {
        limit = Integer.parseInt(speed);
      }
      String devIdno = getGPSDevIdno(vehiIdno);
      
      AjaxDto<StandardDeviceTrack> ajaxDto = this.vehicleGpsService.queryDeviceGps(vehiIdno, DateUtil.StrLongTime2Date(begintime), 
        DateUtil.StrLongTime2Date(endtime), meter, interval, limit, park, 0, 0, null, null, devIdno);
      if (ajaxDto.getPageList() != null) {
        for (int i = 1; i <= ajaxDto.getPageList().size(); i++)
        {
          StandardDeviceTrack track = (StandardDeviceTrack)ajaxDto.getPageList().get(i - 1);
          int j = 0;
          export.setExportData(Integer.valueOf(1 + i));
          
          export.setCellValue(Integer.valueOf(j++), Integer.valueOf(i));
          
          export.setCellValue(Integer.valueOf(j++), track.getVehiIdno());
          
          export.setCellValue(Integer.valueOf(j++), DateUtil.dateSwitchString(new Date(track.getTrackTime())));
          
          export.setCellValue(Integer.valueOf(j++), getPosition(track.getWeiDu(), track.getJingDu(), track.getStatus1()));
          
          export.setCellValue(Integer.valueOf(j++), getSpeedEx(track.getSpeed(), track.getStatus1()));
          
          export.setCellValue(Integer.valueOf(j++), getLiChengEx(track.getLiCheng()));
        }
      }
    }
    catch (Exception ex)
    {
      this.log.error(ex.getMessage(), ex);
    }
  }
  
  protected String genGpstrackTitle()
  {
    String vehiIdno = getRequest().getParameter("vehiIdnos");
    return vehiIdno + " - " + getText("report.track");
  }
}
