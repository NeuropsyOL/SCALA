//  Copyright John Maddock 2007.
//  Use, modification and distribution are subject to the
//  Boost Software License, Version 1.0. (See accompanying file
//  LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)

#ifndef BOOST_MATH_MODF_HPP
#define BOOST_MATH_MODF_HPP

#ifdef _MSC_VER
#pragma once
#endif

#include <lslboost/math/tools/config.hpp>
#include <lslboost/math/special_functions/trunc.hpp>

namespace lslboost{ namespace math{

template <class T, class Policy>
inline T modf(const T& v, T* ipart, const Policy& pol)
{
   *ipart = trunc(v, pol);
   return v - *ipart;
}
template <class T>
inline T modf(const T& v, T* ipart)
{
   return modf(v, ipart, policies::policy<>());
}

template <class T, class Policy>
inline T modf(const T& v, int* ipart, const Policy& pol)
{
   *ipart = itrunc(v, pol);
   return v - *ipart;
}
template <class T>
inline T modf(const T& v, int* ipart)
{
   return modf(v, ipart, policies::policy<>());
}

template <class T, class Policy>
inline T modf(const T& v, long* ipart, const Policy& pol)
{
   *ipart = ltrunc(v, pol);
   return v - *ipart;
}
template <class T>
inline T modf(const T& v, long* ipart)
{
   return modf(v, ipart, policies::policy<>());
}

#ifdef BOOST_HAS_LONG_LONG
template <class T, class Policy>
inline T modf(const T& v, lslboost::long_long_type* ipart, const Policy& pol)
{
   *ipart = lltrunc(v, pol);
   return v - *ipart;
}
template <class T>
inline T modf(const T& v, lslboost::long_long_type* ipart)
{
   return modf(v, ipart, policies::policy<>());
}
#endif

}} // namespaces

#endif // BOOST_MATH_MODF_HPP
