package com.framework.web.dao;

import com.framework.web.dto.AjaxDto;
import com.framework.web.dto.Pagination;
import com.framework.web.dto.QueryScalar;
import java.util.List;

public abstract interface PaginationDao
{
  public abstract Integer getCountByQueryStr(String paramString);
  
  public abstract AjaxDto getPgntByQueryStr(String paramString, Pagination paramPagination);
  
  public abstract AjaxDto getPgntByQueryStrEx(String paramString1, Pagination paramPagination, String paramString2);
  
  public abstract Integer getCountByNativeSql(String paramString);
  
  public abstract List getExtraByNativeSql(String paramString, List<QueryScalar> paramList, Class paramClass);
  
  public abstract AjaxDto getExtraByNativeSqlEx(String paramString1, Pagination paramPagination, List<QueryScalar> paramList, Class paramClass, String paramString2);
  
  public abstract void execNativeSql(String paramString);
}
