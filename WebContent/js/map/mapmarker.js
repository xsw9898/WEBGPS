/********************************************************************************************
 *��ǵ����
 ********************************************************************************************/
function mapmarker(id){
	this.id = Number(id);					//id
	this.name = null;							//����
	this.jindu = null;						//����
	this.weidu = null;						//γ��
	this.typeId = null;						//�������ID
	this.status = null;						//״̬
	this.color = null;            //��ɫ
	this.tabType = null;          //������ͣ��㣬�棬�ߣ�
	this.popMarker = null;
	this.nameMarker = null;				//���Ӳ����
	this.shape = null;						//ͼ�ζ���
	this.listener = null;					//��������
	this.position = null;					//λ�ã����Ʊ�ǩ��λ��
}
mapmarker.prototype.getId = function() {
	return this.id;
};
mapmarker.prototype.setName = function(name){
	this.name = name;
};
mapmarker.prototype.getName = function() {
	return this.name;
};
mapmarker.prototype.getJindu = function() {
	return this.jindu;
};
mapmarker.prototype.getWeidu = function() {
	return this.weidu;
};
mapmarker.prototype.getTypeId = function() {
	return this.typeId;
};
mapmarker.prototype.getAdress = function() {
	return this.adress;
};
mapmarker.prototype.getRemark = function() {
	return this.remark;
};
mapmarker.prototype.getColor = function() {
	return this.color;
};
mapmarker.prototype.getTabType = function() {
	return this.tabType;
};
mapmarker.prototype.setNameMaker = function(maker){
	this.nameMarker = maker;
};
mapmarker.prototype.getNameMaker = function(){
	return this.nameMarker;
};
mapmarker.prototype.setData = function(typeId, jindu, weidu, color, tabType){
	this.typeId = typeId;
	this.jindu=jindu;
	this.weidu=weidu;
	this.color=color;
	this.tabType=tabType;
	this.status=status;
};