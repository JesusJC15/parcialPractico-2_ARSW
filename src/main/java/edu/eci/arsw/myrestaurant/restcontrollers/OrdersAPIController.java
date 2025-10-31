/*
* Copyright (C) 2016 Pivotal Software, Inc.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package edu.eci.arsw.myrestaurant.restcontrollers;

import edu.eci.arsw.myrestaurant.model.Order;
import edu.eci.arsw.myrestaurant.services.RestaurantOrderServicesStub;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import edu.eci.arsw.myrestaurant.beans.impl.BasicBillCalculator;
import java.util.Map;

/**
 *
 * @author hcadavid
 */

@RestController
public class OrdersAPIController {

    private RestaurantOrderServicesStub restaurantOrderServices;

    private final BasicBillCalculator basicBillCalculator;

    @Autowired
    public OrdersAPIController(RestaurantOrderServicesStub restaurantOrderServices, BasicBillCalculator basicBillCalculator) {
        this.restaurantOrderServices = restaurantOrderServices;
        this.basicBillCalculator = basicBillCalculator;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/orders")
    public ResponseEntity<?> getTablesWithOrders() {
        try {
            Set<Integer> orders = restaurantOrderServices.getTablesWithOrders();
            return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/orders/details")
    public ResponseEntity<?> getOrdersWithDetails() {
        try {
            Map<Integer, Map<String, Object>> ordersWithTotals = new ConcurrentHashMap<>();
            for (Integer tableNumber : restaurantOrderServices.getTablesWithOrders()) {
                Order order = restaurantOrderServices.getTableOrder(tableNumber);
                int total = basicBillCalculator.calculateBill(order, restaurantOrderServices.getProductsMap());
                Map<String, Object> orderDetails = new ConcurrentHashMap<>();
                orderDetails.put("products", order.getOrderAmountsMap());
                orderDetails.put("total", total);
                ordersWithTotals.put(tableNumber, orderDetails);
            }
            return new ResponseEntity<>(ordersWithTotals, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
