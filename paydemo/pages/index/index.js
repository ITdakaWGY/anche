//index.js
//获取应用实例
const app = getApp()

Page({
    data: {
        motto: 'Hello World',
        userInfo: {},
        hasUserInfo: false,
        canIUse: wx.canIUse('button.open-type.getUserInfo'),
        payResult: ''
    },
    //事件处理函数
    bindViewTap: function () {
        wx.navigateTo({
            url: '../logs/logs'
        })
    },
    payTest: function () {
        var that = this;
        wx.requestPayment({
            'timeStamp': '1602205478',
            'nonceStr': '4d8785f519944e849c56b1a6fe2ee4f5',
            'package': 'prepay_id=wx090904388587833ab18e7d0d603c750000',
            'signType': 'RSA',
            'paySign': 'ejdrIaJYuUjRir7rXeEFNuPbhDUUss4+bIZU9BiWvLHR17J7SNWFzDNgikwONs8nUGuFbv7Zzn/uJXj0941JGOZV17MqkRyArcRggU5z4bBBKDjBkcbFfwWpUKID6RaqvWYCHlf1eb/hIskg/x2oSzR8ebyXtc9ZQWwp/aNCUqJWudWyDuTsq7Ns7ImkOEapfJdD2q5BW+YaNtfBQpPiP0vi4sl0csuzKOKCSyG49hT4CBwsWL4/zegEmTkPDRhRuYNxAhn3xZdaI3tTyVFcuQXdBr5GNjatgaZ8GoZdKdOLgsfz5KRz0hu3NCl3WXJUzW9CpaG/f/gDnWe9ZlKH8g==',
            'success': function (res) {
                that.setData({
                    payResult: '支付成功'
                })
            },
            'fail': function (res) {
                that.setData({
                    payResult: '支付失败'
                })
            },
            'complete': function (res) { }
        })
    },
    onLoad: function () {
        if (app.globalData.userInfo) {
            this.setData({
                userInfo: app.globalData.userInfo,
                hasUserInfo: true
            })
        } else if (this.data.canIUse) {
            // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
            // 所以此处加入 callback 以防止这种情况
            app.userInfoReadyCallback = res => {
                this.setData({
                    userInfo: res.userInfo,
                    hasUserInfo: true
                })
            }
        } else {
            // 在没有 open-type=getUserInfo 版本的兼容处理
            wx.getUserInfo({
                success: res => {
                    app.globalData.userInfo = res.userInfo
                    this.setData({
                        userInfo: res.userInfo,
                        hasUserInfo: true
                    })
                }
            })
        }
    },
    getUserInfo: function (e) {
        console.log(e)
        app.globalData.userInfo = e.detail.userInfo
        this.setData({
            userInfo: e.detail.userInfo,
            hasUserInfo: true
        })
    }
})