package org.dromara.redisfront.model.turbo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Turbo2<T1,T2> {
    private T1 t1;
    private T2 t2;
}
