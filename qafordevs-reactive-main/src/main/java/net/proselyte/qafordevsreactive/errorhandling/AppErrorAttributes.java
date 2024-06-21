package net.proselyte.qafordevsreactive.errorhandling;


import net.proselyte.qafordevsreactive.exception.ApiException;
import net.proselyte.qafordevsreactive.exception.DeveloperNotFoundException;
import net.proselyte.qafordevsreactive.exception.DeveloperWithEmailAlreadyExistsException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AppErrorAttributes extends DefaultErrorAttributes {//компонент AppErrorAttributes экстендит DefaultErrorAttributes
    //который входит в стандартный набор обработки ошибок спринга

    public AppErrorAttributes() {
        super();
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {//переопределяем метод
        //getErrorAttributes который позволяет присвоить некоторые параметры ошибки которые мы отдадим наружу на основании исключения которое произошло
        var errorAttributes = super.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        var error = getError(request);

        var errorList = new ArrayList<Map<String, Object>>();

        HttpStatus status;
        if (error instanceof DeveloperWithEmailAlreadyExistsException) { //если ошибка которая была перехвачена относится к DeveloperWithEmailAlreadyExistsException
            status = HttpStatus.BAD_REQUEST;//присваиваем этой ошибки 400 BAD_REQUEST
            var errorMap = new LinkedHashMap<String, Object>();
            errorMap.put("code", ((ApiException) error).getErrorCode());//накидываем код ошибки
            errorMap.put("message", error.getMessage());//накидываем сообщение
            errorList.add(errorMap);//и в коллекцию ошибок передаем эту ошибку (эту мапу: var errorMap = new LinkedHashMap<String, Object>(); )
        } else if (error instanceof DeveloperNotFoundException) {//если ошибка которая была перехвачена относится к DeveloperNotFoundException
            status = HttpStatus.NOT_FOUND;//устанавливаем статус NOT_FOUND и ниже все по аналогии
            var errorMap = new LinkedHashMap<String, Object>();
            errorMap.put("code", ((ApiException) error).getErrorCode());
            errorMap.put("message", error.getMessage());
            errorList.add(errorMap);
        } else if (error instanceof ApiException) {
            status = HttpStatus.NOT_FOUND;
            var errorMap = new LinkedHashMap<String, Object>();
            errorMap.put("code", ((ApiException) error).getErrorCode());
            errorMap.put("message", error.getMessage());
            errorList.add(errorMap);
        } else {//если все вышесказанное не подошло
            status = HttpStatus.INTERNAL_SERVER_ERROR;//даем статус самый страшный))) 500
            var message = error.getMessage();
            if (message == null)
                message = error.getClass().getName();

            var errorMap = new LinkedHashMap<String, Object>();
            errorMap.put("code", "INTERNAL_ERROR");
            errorMap.put("message", message);
            errorList.add(errorMap);
        }

        //здесь ошибки кладутся в мапу и отдаются наверх
        var errors = new HashMap<String, Object>();
        errors.put("errors", errorList);
        errorAttributes.put("status", status.value());
        errorAttributes.put("errors", errors);

        return errorAttributes;
    }
}
