package com.wfj.search.online.management.console.service.popularize;

import com.wfj.search.online.common.pojo.popularize.BrandPopularizePositionPojo;

import java.util.List;

/**
 * <br/>create at 15-8-21
 *
 * @author liuxh
 * @since 1.0.0
 */
public interface IBrandPopularizePositionService {
    /**
     * 根据条件分页查询坑位信息
     *
     * @param position 坑位条件
     * @param start    起始
     * @param limit    查询条数
     * @return 坑位列表
     */
    List<BrandPopularizePositionPojo> listWithPage(BrandPopularizePositionPojo position, int start, int limit);

    /**
     * 根据条件查询坑位条数
     *
     * @param position 坑位条件
     * @return 坑位数量
     */
    long brandPositionTotal(BrandPopularizePositionPojo position);

    /**
     * 添加坑位信息
     *
     * @param position 坑位信息
     * @param modifier 修改者
     * @return 添加条数。品牌编号、spu编号、创建操作人为空，不能够添加，返回-1。数据库存在相同品牌编号、spu编号的数据，不允许添加，返回-2。
     */
    int addBrandPosition(BrandPopularizePositionPojo position, String modifier);

    /**
     * 删除坑位信息，修改坑位状态字段status为1。
     *
     * @param position 坑位信息
     * @param modifier 修改者
     * @return 删除条数。品牌编号、spu编号、删除操作人为空，不能够删除，返回-1。数据库不存在相同品牌编号、spu编号的数据，不允许删除，返回-2。
     */
    int deleteBrandPosition(BrandPopularizePositionPojo position, String modifier);
}
