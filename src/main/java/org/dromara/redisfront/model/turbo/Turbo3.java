package org.dromara.redisfront.model.turbo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Turbo3<T1,T2,T3> {
    private T1 t1;
    private T2 t2;
    private T3 t3;

}
