package com.biz.aceras.ordertracking.image_slider

/**
 * Created by eesern_ong on 23/4/2019.
 */
class SlideModel {

    var imageUrl: String? = null
    var imagePath: Int? = 0
    var title: String? = null

    constructor (imageUrl: String) {
        this.imageUrl = imageUrl
    }

    constructor (imagePath: Int) {
        this.imagePath = imagePath
    }

    constructor (imageUrl: String, title: String?) {
        this.imageUrl = imageUrl
        this.title = title
    }

    constructor (imagePath: Int, title: String?) {
        this.imagePath = imagePath
        this.title = title
    }

}