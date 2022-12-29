
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/listers/OrderCards"
import OrderDetail from "./components/listers/OrderDetail"

import PaymentManager from "./components/listers/PaymentCards"
import PaymentDetail from "./components/listers/PaymentDetail"

import ShopManagementManager from "./components/listers/ShopManagementCards"
import ShopManagementDetail from "./components/listers/ShopManagementDetail"

import ShippingManager from "./components/listers/ShippingCards"
import ShippingDetail from "./components/listers/ShippingDetail"

import DeliveryManager from "./components/listers/DeliveryCards"
import DeliveryDetail from "./components/listers/DeliveryDetail"


import StatusViewView from "./components/StatusViewView"
import StatusViewViewDetail from "./components/StatusViewViewDetail"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },
            {
                path: '/orders/:id',
                name: 'OrderDetail',
                component: OrderDetail
            },

            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },
            {
                path: '/payments/:id',
                name: 'PaymentDetail',
                component: PaymentDetail
            },

            {
                path: '/shopManagements',
                name: 'ShopManagementManager',
                component: ShopManagementManager
            },
            {
                path: '/shopManagements/:id',
                name: 'ShopManagementDetail',
                component: ShopManagementDetail
            },

            {
                path: '/shippings',
                name: 'ShippingManager',
                component: ShippingManager
            },
            {
                path: '/shippings/:id',
                name: 'ShippingDetail',
                component: ShippingDetail
            },

            {
                path: '/deliveries',
                name: 'DeliveryManager',
                component: DeliveryManager
            },
            {
                path: '/deliveries/:id',
                name: 'DeliveryDetail',
                component: DeliveryDetail
            },


            {
                path: '/statusViews',
                name: 'StatusViewView',
                component: StatusViewView
            },
            {
                path: '/statusViews/:id',
                name: 'StatusViewViewDetail',
                component: StatusViewViewDetail
            },


    ]
})
