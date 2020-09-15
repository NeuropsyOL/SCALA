
// NO INCLUDE GUARDS, THE HEADER IS INTENDED FOR MULTIPLE INCLUSION

// Copyright Aleksey Gurtovoy 2002-2004
//
// Distributed under the Boost Software License, Version 1.0. 
// (See accompanying file LICENSE_1_0.txt or copy at 
// http://www.lslboost.org/LICENSE_1_0.txt)

#include <lslboost/mpl/int.hpp>
#include <lslboost/mpl/aux_/template_arity_fwd.hpp>
#include <lslboost/mpl/aux_/preprocessor/params.hpp>
#include <lslboost/mpl/aux_/config/lambda.hpp>
#include <lslboost/mpl/aux_/config/overload_resolution.hpp>

#if defined(BOOST_MPL_CFG_NO_FULL_LAMBDA_SUPPORT) \
    && defined(BOOST_MPL_CFG_BROKEN_OVERLOAD_RESOLUTION)
#   define BOOST_TT_AUX_TEMPLATE_ARITY_SPEC(i, name) \
namespace mpl { namespace aux { \
template< BOOST_MPL_PP_PARAMS(i, typename T) > \
struct template_arity< \
          name< BOOST_MPL_PP_PARAMS(i, T) > \
        > \
    : int_<i> \
{ \
}; \
}} \
/**/
#else
#   define BOOST_TT_AUX_TEMPLATE_ARITY_SPEC(i, name) /**/
#endif
