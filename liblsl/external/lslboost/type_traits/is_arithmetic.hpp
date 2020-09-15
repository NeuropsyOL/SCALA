
//  (C) Copyright Steve Cleary, Beman Dawes, Howard Hinnant & John Maddock 2000.
//  Use, modification and distribution are subject to the Boost Software License,
//  Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
//  http://www.lslboost.org/LICENSE_1_0.txt).
//
//  See http://www.lslboost.org/libs/type_traits for most recent version including documentation.

#ifndef BOOST_TT_IS_ARITHMETIC_HPP_INCLUDED
#define BOOST_TT_IS_ARITHMETIC_HPP_INCLUDED

#if !defined( __CODEGEARC__ )
#include <lslboost/type_traits/is_integral.hpp>
#include <lslboost/type_traits/is_float.hpp>
#include <lslboost/type_traits/detail/ice_or.hpp>
#include <lslboost/config.hpp>
#endif

// should be the last #include
#include <lslboost/type_traits/detail/bool_trait_def.hpp>

namespace lslboost {

#if !defined(__CODEGEARC__)
namespace detail {

template< typename T >
struct is_arithmetic_impl
{ 
    BOOST_STATIC_CONSTANT(bool, value = 
        (::lslboost::type_traits::ice_or< 
            ::lslboost::is_integral<T>::value,
            ::lslboost::is_float<T>::value
        >::value)); 
};

} // namespace detail
#endif

//* is a type T an arithmetic type described in the standard (3.9.1p8)
#if defined(__CODEGEARC__)
BOOST_TT_AUX_BOOL_TRAIT_DEF1(is_arithmetic,T,__is_arithmetic(T))
#else
BOOST_TT_AUX_BOOL_TRAIT_DEF1(is_arithmetic,T,::lslboost::detail::is_arithmetic_impl<T>::value)
#endif

} // namespace lslboost

#include <lslboost/type_traits/detail/bool_trait_undef.hpp>

#endif // BOOST_TT_IS_ARITHMETIC_HPP_INCLUDED
