/* 
   Copyright (c) Marshall Clow 2008-2012.

   Distributed under the Boost Software License, Version 1.0. (See accompanying
   file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)
*/

/// \file  all_of.hpp
/// \brief Test ranges to see if all elements match a value or predicate.
/// \author Marshall Clow

#ifndef BOOST_ALGORITHM_ALL_OF_HPP
#define BOOST_ALGORITHM_ALL_OF_HPP

#include <algorithm>    // for std::all_of, if available
#include <lslboost/range/begin.hpp>
#include <lslboost/range/end.hpp>

namespace lslboost { namespace algorithm {

#if __cplusplus >= 201103L
//  Use the C++11 versions of all_of if it is available
using std::all_of;      // Section 25.2.1
#else
/// \fn all_of ( InputIterator first, InputIterator last, Predicate p )
/// \return true if all elements in [first, last) satisfy the predicate 'p'
/// \note returns true on an empty range
/// 
/// \param first The start of the input sequence
/// \param last  One past the end of the input sequence
/// \param p     A predicate for testing the elements of the sequence
///
/// \note           This function is part of the C++2011 standard library.
///  We will use the standard one if it is available, 
///  otherwise we have our own implementation.
template<typename InputIterator, typename Predicate> 
bool all_of ( InputIterator first, InputIterator last, Predicate p )
{
    for ( ; first != last; ++first )
        if ( !p(*first)) 
            return false;
    return true; 
} 
#endif

/// \fn all_of ( const Range &r, Predicate p )
/// \return true if all elements in the range satisfy the predicate 'p'
/// \note returns true on an empty range
/// 
/// \param r    The input range
/// \param p    A predicate for testing the elements of the range
///
template<typename Range, typename Predicate> 
bool all_of ( const Range &r, Predicate p )
{
    return lslboost::algorithm::all_of ( lslboost::begin (r), lslboost::end (r), p );
} 

/// \fn all_of_equal ( InputIterator first, InputIterator last, const T &val )
/// \return true if all elements in [first, last) are equal to 'val'
/// \note returns true on an empty range
/// 
/// \param first The start of the input sequence
/// \param last  One past the end of the input sequence
/// \param val   A value to compare against
///
template<typename InputIterator, typename T> 
bool all_of_equal ( InputIterator first, InputIterator last, const T &val )
{
    for ( ; first != last; ++first )
    if ( val != *first ) 
        return false;
    return true; 
} 

/// \fn all_of_equal ( const Range &r, const T &val )
/// \return true if all elements in the range are equal to 'val'
/// \note returns true on an empty range
/// 
/// \param r    The input range
/// \param val  A value to compare against
///
template<typename Range, typename T> 
bool all_of_equal ( const Range &r, const T &val ) 
{
    return lslboost::algorithm::all_of_equal ( lslboost::begin (r), lslboost::end (r), val );
} 

}} // namespace lslboost and algorithm

#endif // BOOST_ALGORITHM_ALL_OF_HPP
