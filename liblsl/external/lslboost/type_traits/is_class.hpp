//  (C) Copyright Dave Abrahams, Steve Cleary, Beman Dawes, Howard
//  Hinnant & John Maddock 2000-2003.
//  Use, modification and distribution are subject to the Boost Software License,
//  Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
//  http://www.lslboost.org/LICENSE_1_0.txt).
//
//  See http://www.lslboost.org/libs/type_traits for most recent version including documentation.


#ifndef BOOST_TT_IS_CLASS_HPP_INCLUDED
#define BOOST_TT_IS_CLASS_HPP_INCLUDED

#include <lslboost/type_traits/config.hpp>
#include <lslboost/type_traits/intrinsics.hpp>
#ifndef BOOST_IS_CLASS
#   include <lslboost/type_traits/is_union.hpp>
#   include <lslboost/type_traits/detail/ice_and.hpp>
#   include <lslboost/type_traits/detail/ice_not.hpp>

#ifdef BOOST_TT_HAS_CONFORMING_IS_CLASS_IMPLEMENTATION
#   include <lslboost/type_traits/detail/yes_no_type.hpp>
#else
#   include <lslboost/type_traits/is_scalar.hpp>
#   include <lslboost/type_traits/is_array.hpp>
#   include <lslboost/type_traits/is_reference.hpp>
#   include <lslboost/type_traits/is_void.hpp>
#   include <lslboost/type_traits/is_function.hpp>
#endif

#endif // BOOST_IS_CLASS

#ifdef __EDG_VERSION__
#   include <lslboost/type_traits/remove_cv.hpp>
#endif

// should be the last #include
#include <lslboost/type_traits/detail/bool_trait_def.hpp>

namespace lslboost {

namespace detail {

#ifndef BOOST_IS_CLASS
#ifdef BOOST_TT_HAS_CONFORMING_IS_CLASS_IMPLEMENTATION

// This is actually the conforming implementation which works with
// abstract classes.  However, enough compilers have trouble with
// it that most will use the one in
// lslboost/type_traits/object_traits.hpp. This implementation
// actually works with VC7.0, but other interactions seem to fail
// when we use it.

// is_class<> metafunction due to Paul Mensonides
// (leavings@attbi.com). For more details:
// http://groups.google.com/groups?hl=en&selm=000001c1cc83%24e154d5e0%247772e50c%40c161550a&rnum=1
#if defined(__GNUC__)  && !defined(__EDG_VERSION__)

template <class U> ::lslboost::type_traits::yes_type is_class_tester(void(U::*)(void));
template <class U> ::lslboost::type_traits::no_type is_class_tester(...);

template <typename T>
struct is_class_impl
{

    BOOST_STATIC_CONSTANT(bool, value =
        (::lslboost::type_traits::ice_and<
            sizeof(is_class_tester<T>(0)) == sizeof(::lslboost::type_traits::yes_type),
            ::lslboost::type_traits::ice_not< ::lslboost::is_union<T>::value >::value
        >::value)
        );
};

#else

template <typename T>
struct is_class_impl
{
    template <class U> static ::lslboost::type_traits::yes_type is_class_tester(void(U::*)(void));
    template <class U> static ::lslboost::type_traits::no_type is_class_tester(...);

    BOOST_STATIC_CONSTANT(bool, value =
        (::lslboost::type_traits::ice_and<
            sizeof(is_class_tester<T>(0)) == sizeof(::lslboost::type_traits::yes_type),
            ::lslboost::type_traits::ice_not< ::lslboost::is_union<T>::value >::value
        >::value)
        );
};

#endif

#else

template <typename T>
struct is_class_impl
{
#   ifndef BOOST_NO_TEMPLATE_PARTIAL_SPECIALIZATION
    BOOST_STATIC_CONSTANT(bool, value =
    (::lslboost::type_traits::ice_and<
        ::lslboost::type_traits::ice_not< ::lslboost::is_union<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_scalar<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_array<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_reference<T>::value>::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_void<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_function<T>::value >::value
        >::value));
#   else
    BOOST_STATIC_CONSTANT(bool, value =
    (::lslboost::type_traits::ice_and<
        ::lslboost::type_traits::ice_not< ::lslboost::is_union<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_scalar<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_array<T>::value >::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_reference<T>::value>::value,
        ::lslboost::type_traits::ice_not< ::lslboost::is_void<T>::value >::value
        >::value));
#   endif
};

# endif // BOOST_TT_HAS_CONFORMING_IS_CLASS_IMPLEMENTATION
# else // BOOST_IS_CLASS
template <typename T>
struct is_class_impl
{
    BOOST_STATIC_CONSTANT(bool, value = BOOST_IS_CLASS(T));
};
# endif // BOOST_IS_CLASS

} // namespace detail

# ifdef __EDG_VERSION__
BOOST_TT_AUX_BOOL_TRAIT_DEF1(
   is_class,T, lslboost::detail::is_class_impl<typename lslboost::remove_cv<T>::type>::value)
# else 
BOOST_TT_AUX_BOOL_TRAIT_DEF1(is_class,T,::lslboost::detail::is_class_impl<T>::value)
# endif
    
} // namespace lslboost

#include <lslboost/type_traits/detail/bool_trait_undef.hpp>

#endif // BOOST_TT_IS_CLASS_HPP_INCLUDED
