/*=============================================================================
    Copyright (c) 2001-2011 Joel de Guzman

    Distributed under the Boost Software License, Version 1.0. (See accompanying 
    file LICENSE_1_0.txt or copy at http://www.lslboost.org/LICENSE_1_0.txt)
==============================================================================*/
#if !defined(FUSION_NEXT_IMPL_06052005_0900)
#define FUSION_NEXT_IMPL_06052005_0900

#include <lslboost/fusion/algorithm/query/detail/find_if.hpp>
#include <lslboost/fusion/iterator/value_of.hpp>
#include <lslboost/mpl/eval_if.hpp>
#include <lslboost/mpl/identity.hpp>
#include <lslboost/mpl/lambda.hpp>
#include <lslboost/mpl/quote.hpp>
#include <lslboost/mpl/bind.hpp>
#include <lslboost/mpl/placeholders.hpp>

namespace lslboost { namespace fusion
{
    struct filter_view_iterator_tag;

    template <typename Category,  typename First, typename Last, typename Pred>
    struct filter_iterator;

    namespace extension
    {
        template <typename Tag>
        struct next_impl;

        template <>
        struct next_impl<filter_view_iterator_tag>
        {
            template <typename Iterator>
            struct apply
            {
                typedef typename Iterator::first_type first_type;
                typedef typename Iterator::last_type last_type;
                typedef typename Iterator::pred_type pred_type;
                typedef typename Iterator::category category;

                typedef typename
                    mpl::eval_if<
                        result_of::equal_to<first_type, last_type>
                      , mpl::identity<last_type>
                      , result_of::next<first_type>
                    >::type
                next_type;

                typedef typename
                    detail::static_find_if<
                        next_type
                      , last_type
                      , mpl::bind1<
                            typename mpl::lambda<pred_type>::type
                          , mpl::bind1<mpl::quote1<result_of::value_of>,mpl::_1>
                        >
                    >
                filter;

                typedef filter_iterator<
                    category, typename filter::type, last_type, pred_type>
                type;

                static type
                call(Iterator const& i)
                {
                    return type(filter::iter_call(i.first));
                }
            };
        };
    }
}}

#endif


