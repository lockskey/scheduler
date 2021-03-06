package com.jinhui.scheduler.biz.zlrt.header;


import com.jinhui.scheduler.biz.zlrt.common.ZlrtFileConstants;
import com.jinhui.scheduler.biz.zlrt.utils.ConvertorUtils;
import com.jinhui.scheduler.data.core.mapper.zlrt.InvestorTransMapperZlrt;
import com.jinhui.scheduler.domain.common.ChannelCode;
import com.jinhui.scheduler.domain.zlrt.InvestorTrans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.List;

/**
 * Created by wsc on 2017/05/21
 */
public class ZlrtPurchaseFileHeaderCallback implements FlatFileHeaderCallback {
    private final static Logger logger = LoggerFactory.getLogger(ZlrtPurchaseFileHeaderCallback.class);
    @Autowired
    JobExplorerFactoryBean jobExplorer;
    @Autowired
    InvestorTransMapperZlrt investorTransMapperZlrt;
    @Override
    public void writeHeader(Writer writer) throws IOException {
        String applyDate = "";
        String batchCode = "";
        String zlInstuId = "";
        try {
            List<JobInstance> list = jobExplorer.getObject().findJobInstancesByJobName("zlrtBankJob",0,1);
            if(list!= null && !list.isEmpty()) {
                List<JobExecution> jobExecutions = jobExplorer.getObject().getJobExecutions(list.get(0));
                if(jobExecutions != null && !jobExecutions.isEmpty()) {
                    JobParameters jobParameters = jobExecutions.get(0).getJobParameters();
                    applyDate = jobParameters.getString("applyDate");
                    batchCode = jobParameters.getString("batchCode");
                    zlInstuId = jobParameters.getString("zlInstuId");
                }
            }
        } catch (Exception e) {
            logger.info("获取确认日期失败",e);
        }
        File file = ResourceUtils.getFile(ZlrtFileConstants.HEADERFILEPATH_Purchase);//收益文件头信息
        String result= new String();
        InvestorTrans investorTrans = investorTransMapperZlrt.findPurchaseCountSum(batchCode,zlInstuId);
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = "";
            while((s = br.readLine())!=null){

                result = s.replaceAll("zlInstuId",zlInstuId)
                          .replaceAll("applyDate",applyDate)
                          .replaceAll("count",investorTrans.getPurchaseCount())
                          .replaceAll("totalAmt", String.valueOf(ConvertorUtils.convertToStrMulOneHundred(investorTrans.getPurchaseSum()).toBigInteger()));
            }
            br.close();
        }catch(Exception e){
            logger.info("文件生成出错了",e);
        }
        writer.write(result);
    }
}
