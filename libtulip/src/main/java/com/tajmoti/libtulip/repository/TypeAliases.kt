package com.tajmoti.libtulip.repository

import arrow.core.Either
import com.tajmoti.libtulip.model.info.StreamableInfo
import com.tajmoti.libtulip.model.stream.StreamableInfoWithLinks

typealias StreamsResult = Either<StreamableInfo?, StreamableInfoWithLinks>