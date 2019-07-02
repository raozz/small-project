package com.uc56.uop.taobao.message.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.taobao.logistics.mety.model.FieldInfo;
import com.taobao.logistics.mety.model.Response;
import com.taobao.logistics.mety.model.ResponsesModel;
import com.taobao.logistics.mety.model.UpdateInfoModel;
import com.uc56.uop.taobao.common.InfoType;
import com.uc56.uop.taobao.common.OrderStatus;
import com.uc56.uop.taobao.common.OrderType;
import com.uc56.uop.taobao.common.OrderUpdateType;
import com.uc56.uop.taobao.common.SystemError;
import com.uc56.uop.taobao.entity.TaobaoUpdateInfo;

public class UpdateInfoMessage extends AbstractMessage<UpdateInfoModel> {
	// private TaobaoUpdateInfo updateInfo;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5026111045654063923L;

	public UpdateInfoMessage() {
		this.model = new UpdateInfoModel();
	}

	public UpdateInfoMessage(TaobaoUpdateInfo updateInfo) {
		this.model = new UpdateInfoModel();
		FieldInfo field = null;
		if (updateInfo != null) {
			this.model.setLogisticProviderID(updateInfo.getLogisticProviderId());
			this.model.setEcCompanyId(updateInfo.getEcCompanyId());
			field = new FieldInfo();
			field.setTxLogisticID(updateInfo.getLogisticId());
			// 如果是淘宝订单,将大写的status转成小写.
			if (OrderType.TaobaoOrder.title().equals(updateInfo.getOrderType())
					&& InfoType.STATUS.code().equals(updateInfo.getInfoType())) {
				// 揽件成功时,发送更新面单的操作.
				if (OrderStatus.GOT.code().equals(updateInfo.getInfoContent())) {
					field.setFieldName(OrderUpdateType.MAIL_NO.code());
					field.setFieldValue(updateInfo.getBillCode());
				} else {
					field.setFieldName(OrderUpdateType.STATUS.code());
					field.setFieldValue(updateInfo.getInfoContent());
				}
			} else {
				field.setFieldName(updateInfo.getInfoType());
				field.setFieldValue(updateInfo.getInfoContent());
			}
			field.setRemark(updateInfo.getRemark());
			this.model.addField(field);
		}
	}

	public UpdateInfoMessage(String logisticProviderId, String ecCompanyId, String logisticId, String infoType,
			String infoContent, String remark) {
		this.model = new UpdateInfoModel();
		this.model.setLogisticProviderID(logisticProviderId);
		this.model.setEcCompanyId(ecCompanyId);
		FieldInfo field = new FieldInfo();
		field.setTxLogisticID(logisticId);
		field.setFieldName(infoType);
		field.setFieldValue(infoContent);
		field.setRemark(remark);
		this.model.addField(field);
	}

	@Override
	public ResponsesModel validate() {
		ResponsesModel responsesModel = new ResponsesModel();
		responsesModel.setSuccess(true);
		Response response = null;
		if (this.model == null) {
			response = new Response();
			response.setSuccess(false);
			response.setReason(SystemError.SYS_ERROR.errorCode());
			responsesModel.addResponse(response);
			responsesModel.setSuccess(false);
			responsesModel.addResponse(response);
		} else {
			responsesModel.setLogisticProviderID(this.model.getLogisticProviderID());
			List<FieldInfo> fieldList = this.model.getFieldList();
			if (StringUtils.isEmpty(this.model.getLogisticProviderID()) || fieldList == null || fieldList.isEmpty()) {
				response = new Response();
				response.setSuccess(false);
				response.setReason(SystemError.INVALID_XML.errorCode());
				responsesModel.addResponse(response);
				responsesModel.setSuccess(false);
				return responsesModel;
			}
			FieldInfo field = null;
			for (int i = 0; i < fieldList.size(); i++) {
				field = fieldList.get(i);
				if (StringUtils.isEmpty(field.getTxLogisticID()) || StringUtils.isEmpty(field.getFieldName())) {
					response = new Response();
					response.setSuccess(false);
					response.setFieldName(field.getFieldName());
					response.setReason(SystemError.INVALID_XML.errorCode());
					responsesModel.addResponse(response);
				} else if (!OrderUpdateType.containCode(field.getFieldName())) {
					response = new Response();
					response.setSuccess(false);
					response.setFieldName(field.getFieldName());
					response.setReason(SystemError.INVALID_INFO_TYPE.errorCode());
					responsesModel.addResponse(response);
				} else {
					response = new Response();
					response.setSuccess(true);
					responsesModel.addResponse(response);
				}
			}
		}

		return responsesModel;
	}

	/**
	 * @return the updateInfo
	 */
	public List<TaobaoUpdateInfo> getUpdateInfos() {
		List<TaobaoUpdateInfo> taobaoUpdateList = new ArrayList<TaobaoUpdateInfo>();
		if (this.model == null) {
			return taobaoUpdateList;
		}
		
		List<FieldInfo> fieldList = this.model.getFieldList();
		if (fieldList == null || fieldList.isEmpty()) {
			return taobaoUpdateList;
		}
		TaobaoUpdateInfo updateInfo = null;
		FieldInfo field = null;
		for (int i = 0; i < fieldList.size(); i++) {
			field = fieldList.get(i);
			updateInfo = new TaobaoUpdateInfo();
			updateInfo.setLogisticProviderId(this.model.getLogisticProviderID());
			updateInfo.setEcCompanyId(this.model.getEcCompanyId());
			updateInfo.setLogisticId(field.getTxLogisticID());
			updateInfo.setInfoType(field.getFieldName());
			updateInfo.setInfoContent(field.getFieldValue());
			updateInfo.setRemark(field.getRemark());
//			updateInfo.setBillCode(this.model.getMailNo());
//			updateInfo.setName(this.model.getName());
//			if (this.model.getAcceptTime() != null) {
//				updateInfo.setAcceptTime(new Timestamp(this.model.getAcceptTime().getTime()));
//			}
//			if (TrsConstant.TYPE_COD.equals(this.getType())) {
//				updateInfo.setBizType(0);
//			}
			taobaoUpdateList.add(updateInfo);
		}
		return taobaoUpdateList;
	}

}
