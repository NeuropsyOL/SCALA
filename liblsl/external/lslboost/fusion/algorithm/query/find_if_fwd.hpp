/*=============================================================================
    Copyright (c) 2011 Eric Niebler

    Distributed under the Boost Software License, Version 1.0. (See accompanying 
    file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)
==============================================================================*/
#if !defined(BOOST_FUSION_FIND_IF_FWD_HPP_INCLUDED)
#define BOOST_FUSION_FIND_IF_FWD_HPP_INCLUDED

#include <lslboost/utility/enable_if.hpp>
#include <lslboost/type_traits/is_const.hpp>

// Forward declaration of find_if algorithm
namespace lslboost { namespace fusion
{
    namespace result_of
    {
        template <typename Sequence, typename Pred>
        struct find_if;
    }

    template <typename Pred, typename Sequence>
    typename 
        lazy_disable_if<
            is_const<Sequence>
          , result_of::find_if<Sequence, Pred>
        >::type
    find_if(Sequence& seq);

    template <typename Pred, typename Sequence>
    typename result_of::find_if<Sequence const, Pred>::type const
    find_if(Sequence const& seq);
}}

#endif
