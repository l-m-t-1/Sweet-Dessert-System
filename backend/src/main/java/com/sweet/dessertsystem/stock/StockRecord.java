package com.sweet.dessertsystem.stock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_record")
public class StockRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dessertId;
    private Long orderId;
    private Integer changeQuantity;
    private Integer beforeStock;
    private Integer afterStock;
    private String type;
    private String remark;
    private LocalDateTime createTime;
}
