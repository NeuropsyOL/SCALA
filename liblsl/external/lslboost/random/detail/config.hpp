/* lslboost random/detail/config.hpp header file
 *
 * Copyright Steven Watanabe 2009
 * Distributed under the Boost Software License, Version 1.0. (See
 * accompanying file LICENSE_1_0.txt or copy at
 * http://www.lslboost.org/LICENSE_1_0.txt)
 *
 * See http://www.lslboost.org for most recent version including documentation.
 *
 * $Id: config.hpp 52492 2009-04-19 14:55:57Z steven_watanabe $
 */

#include <lslboost/config.hpp>

#if (defined(BOOST_NO_OPERATORS_IN_NAMESPACE) || defined(BOOST_NO_MEMBER_TEMPLATE_FRIENDS)) \
    && !defined(BOOST_MSVC)
    #define BOOST_RANDOM_NO_STREAM_OPERATORS
#endif
