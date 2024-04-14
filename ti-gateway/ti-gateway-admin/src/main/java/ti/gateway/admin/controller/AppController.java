package ti.gateway.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ti.gateway.admin.base.ErrorCode;
import ti.gateway.admin.base.Page;
import ti.gateway.admin.base.Result;
import ti.gateway.admin.controller.param.GwAppInfoParam;
import ti.gateway.admin.service.IGwAppInfoService;
import ti.gateway.admin.service.dto.GwAppInfnDto;
import ti.gateway.admin.service.entity.GwAppInfoEt;
import ti.gateway.base.util.BeanCopierUtils;

/**
 * 网关应用接口
 */
@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private IGwAppInfoService iGwAppInfoService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return "app/list";
    }

    /**
     * 获取网关列表
     *
     * @param gwAppInfoParam
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/queryGwAppInfos", method = RequestMethod.GET)
    public Result<Page<GwAppInfoEt>> queryGwAppInfos(@Validated GwAppInfoParam gwAppInfoParam) {
        GwAppInfnDto gwAppInfnDto = BeanCopierUtils.copierTargetBean(gwAppInfoParam, GwAppInfoParam.class, GwAppInfnDto.class);
        Page<GwAppInfoEt> gwAppInfoEtPage = iGwAppInfoService.queryGwAppInfos(gwAppInfnDto,
                gwAppInfoParam.getCurrentPage(), gwAppInfoParam.getPageSize());
        return new Result<>(ErrorCode.Ok.getValue(), "Success", gwAppInfoEtPage);
    }

}
