#ifndef BOOST_ARCHIVE_DETAIL_ISERIALIZER_HPP
#define BOOST_ARCHIVE_DETAIL_ISERIALIZER_HPP

// MS compatible compilers support #pragma once
#if defined(_MSC_VER) && (_MSC_VER >= 1020)
# pragma once
#pragma inline_depth(511)
#pragma inline_recursion(on)
#endif

#if defined(__MWERKS__)
#pragma inline_depth(511)
#endif

/////////1/////////2/////////3/////////4/////////5/////////6/////////7/////////8
// iserializer.hpp: interface for serialization system.

// (C) Copyright 2002 Robert Ramey - http://www.rrsd.com . 
// Use, modification and distribution is subject to the Boost Software
// License, Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.lslboost.org/LICENSE_1_0.txt)

//  See http://www.lslboost.org for updates, documentation, and revision history.

#include <new>     // for placement new
#include <memory>  // for auto_ptr
#include <cstddef> // size_t, NULL

#include <lslboost/config.hpp>
#include <lslboost/detail/workaround.hpp>
#if defined(BOOST_NO_STDC_NAMESPACE)
namespace std{ 
    using ::size_t; 
} // namespace std
#endif

#include <lslboost/static_assert.hpp>

#include <lslboost/mpl/eval_if.hpp>
#include <lslboost/mpl/identity.hpp>
#include <lslboost/mpl/greater_equal.hpp>
#include <lslboost/mpl/equal_to.hpp>
#include <lslboost/mpl/bool.hpp>
#include <lslboost/detail/no_exceptions_support.hpp>

#ifndef BOOST_SERIALIZATION_DEFAULT_TYPE_INFO   
    #include <lslboost/serialization/extended_type_info_typeid.hpp>   
#endif
#include <lslboost/serialization/throw_exception.hpp>
#include <lslboost/serialization/smart_cast.hpp>
#include <lslboost/serialization/static_warning.hpp>

#include <lslboost/type_traits/is_pointer.hpp>
#include <lslboost/type_traits/is_enum.hpp>
#include <lslboost/type_traits/is_const.hpp>
#include <lslboost/type_traits/remove_const.hpp>
#include <lslboost/type_traits/remove_extent.hpp>
#include <lslboost/type_traits/is_polymorphic.hpp>

#include <lslboost/serialization/assume_abstract.hpp>

#define DONT_USE_HAS_NEW_OPERATOR (                    \
    defined(__BORLANDC__)                              \
    || BOOST_WORKAROUND(__IBMCPP__, < 1210)            \
    || defined(BOOST_MSVC) && (BOOST_MSVC <= 1300)     \
    || defined(__SUNPRO_CC) && (__SUNPRO_CC < 0x590)   \
)

#if ! DONT_USE_HAS_NEW_OPERATOR
#include <lslboost/type_traits/has_new_operator.hpp>
#endif

#include <lslboost/serialization/serialization.hpp>
#include <lslboost/serialization/version.hpp>
#include <lslboost/serialization/level.hpp>
#include <lslboost/serialization/tracking.hpp>
#include <lslboost/serialization/type_info_implementation.hpp>
#include <lslboost/serialization/nvp.hpp>
#include <lslboost/serialization/void_cast.hpp>
#include <lslboost/serialization/array.hpp>
#include <lslboost/serialization/collection_size_type.hpp>
#include <lslboost/serialization/singleton.hpp>
#include <lslboost/serialization/wrapper.hpp>

// the following is need only for dynamic cast of polymorphic pointers
#include <lslboost/archive/archive_exception.hpp>
#include <lslboost/archive/detail/basic_iarchive.hpp>
#include <lslboost/archive/detail/basic_iserializer.hpp>
#include <lslboost/archive/detail/basic_pointer_iserializer.hpp>
#include <lslboost/archive/detail/archive_serializer_map.hpp>
#include <lslboost/archive/detail/check.hpp>

namespace lslboost {

namespace serialization {
    class extended_type_info;
} // namespace serialization

namespace archive {

// an accessor to permit friend access to archives.  Needed because
// some compilers don't handle friend templates completely
class load_access {
public:
    template<class Archive, class T>
    static void load_primitive(Archive &ar, T &t){
        ar.load(t);
    }
};

namespace detail {

#ifdef BOOST_MSVC
#  pragma warning(push)
#  pragma warning(disable : 4511 4512)
#endif

template<class Archive, class T>
class iserializer : public basic_iserializer
{
private:
    virtual void destroy(/*const*/ void *address) const {
        lslboost::serialization::access::destroy(static_cast<T *>(address));
    }
protected:
    // protected constructor since it's always created by singleton
    explicit iserializer() :
        basic_iserializer(
            lslboost::serialization::singleton<
                BOOST_DEDUCED_TYPENAME 
                lslboost::serialization::type_info_implementation< T >::type
            >::get_const_instance()
        )
    {}
public:
    virtual BOOST_DLLEXPORT void load_object_data(
        basic_iarchive & ar,
        void *x, 
        const unsigned int file_version
    ) const BOOST_USED;
    virtual bool class_info() const {
        return lslboost::serialization::implementation_level< T >::value 
            >= lslboost::serialization::object_class_info;
    }
    virtual bool tracking(const unsigned int /* flags */) const {
        return lslboost::serialization::tracking_level< T >::value 
                == lslboost::serialization::track_always
            || ( lslboost::serialization::tracking_level< T >::value 
                == lslboost::serialization::track_selectively
                && serialized_as_pointer());
    }
    virtual version_type version() const {
        return version_type(::lslboost::serialization::version< T >::value);
    }
    virtual bool is_polymorphic() const {
        return lslboost::is_polymorphic< T >::value;
    }
    virtual ~iserializer(){};
};

#ifdef BOOST_MSVC
#  pragma warning(pop)
#endif

template<class Archive, class T>
BOOST_DLLEXPORT void iserializer<Archive, T>::load_object_data(
    basic_iarchive & ar,
    void *x, 
    const unsigned int file_version
) const {
    // note: we now comment this out. Before we permited archive
    // version # to be very large.  Now we don't.  To permit
    // readers of these old archives, we have to suppress this 
    // code.  Perhaps in the future we might re-enable it but
    // permit its suppression with a runtime switch.
    #if 0
    // trap case where the program cannot handle the current version
    if(file_version > static_cast<const unsigned int>(version()))
        lslboost::serialization::throw_exception(
            archive::archive_exception(
                lslboost::archive::archive_exception::unsupported_class_version,
                get_debug_info()
            )
        );
    #endif
    // make sure call is routed through the higest interface that might
    // be specialized by the user.
    lslboost::serialization::serialize_adl(
        lslboost::serialization::smart_cast_reference<Archive &>(ar),
        * static_cast<T *>(x), 
        file_version
    );
}

#ifdef BOOST_MSVC
#  pragma warning(push)
#  pragma warning(disable : 4511 4512)
#endif

template<class Archive, class T>
class pointer_iserializer :
    public basic_pointer_iserializer
{
private:
    virtual const basic_iserializer & get_basic_serializer() const {
        return lslboost::serialization::singleton<
            iserializer<Archive, T>
        >::get_const_instance();
    }
    BOOST_DLLEXPORT virtual void load_object_ptr(
        basic_iarchive & ar, 
        void * & x,
        const unsigned int file_version
    ) const BOOST_USED;
protected:
    // this should alway be a singleton so make the constructor protected
    pointer_iserializer();
    ~pointer_iserializer();
};

#ifdef BOOST_MSVC
#  pragma warning(pop)
#endif

// note trick to be sure that operator new is using class specific
// version if such exists. Due to Peter Dimov.
// note: the following fails if T has no default constructor.
// otherwise it would have been ideal
//struct heap_allocator : public T 
//{
//    T * invoke(){
//        return ::new(sizeof(T));
//    }
//}

template<class T>
struct heap_allocator
{
    // lslboost::has_new_operator< T > doesn't work on these compilers
    #if DONT_USE_HAS_NEW_OPERATOR
        // This doesn't handle operator new overload for class T
        static T * invoke(){
            return static_cast<T *>(operator new(sizeof(T)));
        }
    #else
        struct has_new_operator {
            static T* invoke() {
                return static_cast<T *>((T::operator new)(sizeof(T)));
            }
        };
        struct doesnt_have_new_operator {
            static T* invoke() {
                return static_cast<T *>(operator new(sizeof(T)));
            }
        };
        static T * invoke() {
            typedef BOOST_DEDUCED_TYPENAME
                mpl::eval_if<
                    lslboost::has_new_operator< T >,
                    mpl::identity<has_new_operator >,
                    mpl::identity<doesnt_have_new_operator >    
                >::type typex;
            return typex::invoke();
        }
    #endif
};

// due to Martin Ecker
template <typename T>
class auto_ptr_with_deleter
{
public:
    explicit auto_ptr_with_deleter(T* p) :
        m_p(p)
    {}
    ~auto_ptr_with_deleter(){
        if (m_p)
            lslboost::serialization::access::destroy(m_p);
    }
    T* get() const {
        return m_p;
    }

    T* release() {
        T* p = m_p;
        m_p = NULL;
        return p;
    }
private:
    T* m_p;
};

// note: BOOST_DLLEXPORT is so that code for polymorphic class
// serialized only through base class won't get optimized out
template<class Archive, class T>
BOOST_DLLEXPORT void pointer_iserializer<Archive, T>::load_object_ptr(
    basic_iarchive & ar, 
    void * & x,
    const unsigned int file_version
) const
{
    Archive & ar_impl = 
        lslboost::serialization::smart_cast_reference<Archive &>(ar);

    auto_ptr_with_deleter< T > ap(heap_allocator< T >::invoke());
    if(NULL == ap.get())
        lslboost::serialization::throw_exception(std::bad_alloc()) ;

    T * t = ap.get();
    x = t;

    // catch exception during load_construct_data so that we don't
    // automatically delete the t which is most likely not fully
    // constructed
    BOOST_TRY {
        // this addresses an obscure situtation that occurs when 
        // load_constructor de-serializes something through a pointer.
        ar.next_object_pointer(t);
        lslboost::serialization::load_construct_data_adl<Archive, T>(
            ar_impl,
            t, 
            file_version
        );
    }
    BOOST_CATCH(...){
        ap.release();
        BOOST_RETHROW;
    }
    BOOST_CATCH_END

    ar_impl >> lslboost::serialization::make_nvp(NULL, * t);
    ap.release();
}

template<class Archive, class T>
pointer_iserializer<Archive, T>::pointer_iserializer() :
    basic_pointer_iserializer(
        lslboost::serialization::singleton<
            BOOST_DEDUCED_TYPENAME 
            lslboost::serialization::type_info_implementation< T >::type
        >::get_const_instance()
    )
{
    lslboost::serialization::singleton<
        iserializer<Archive, T>
    >::get_mutable_instance().set_bpis(this);
    archive_serializer_map<Archive>::insert(this);
}

template<class Archive, class T>
pointer_iserializer<Archive, T>::~pointer_iserializer(){
    archive_serializer_map<Archive>::erase(this);
}

template<class Archive>
struct load_non_pointer_type {
    // note this bounces the call right back to the archive
    // with no runtime overhead
    struct load_primitive {
        template<class T>
        static void invoke(Archive & ar, T & t){
            load_access::load_primitive(ar, t);
        }
    };
    // note this bounces the call right back to the archive
    // with no runtime overhead
    struct load_only {
        template<class T>
        static void invoke(Archive & ar, const T & t){
            // short cut to user's serializer
            // make sure call is routed through the higest interface that might
            // be specialized by the user.
            lslboost::serialization::serialize_adl(
                ar, 
                const_cast<T &>(t), 
                lslboost::serialization::version< T >::value
            );
        }
    };

    // note this save class information including version
    // and serialization level to the archive
    struct load_standard {
        template<class T>
        static void invoke(Archive &ar, const T & t){
            void * x = & const_cast<T &>(t);
            ar.load_object(
                x, 
                lslboost::serialization::singleton<
                    iserializer<Archive, T>
                >::get_const_instance()
            );
        }
    };

    struct load_conditional {
        template<class T>
        static void invoke(Archive &ar, T &t){
            //if(0 == (ar.get_flags() & no_tracking))
                load_standard::invoke(ar, t);
            //else
            //    load_only::invoke(ar, t);
        }
    };

    template<class T>
    static void invoke(Archive & ar, T &t){
        typedef BOOST_DEDUCED_TYPENAME mpl::eval_if<
                // if its primitive
                mpl::equal_to<
                    lslboost::serialization::implementation_level< T >,
                    mpl::int_<lslboost::serialization::primitive_type>
                >,
                mpl::identity<load_primitive>,
            // else
            BOOST_DEDUCED_TYPENAME mpl::eval_if<
            // class info / version
            mpl::greater_equal<
                        lslboost::serialization::implementation_level< T >,
                        mpl::int_<lslboost::serialization::object_class_info>
                    >,
            // do standard load
            mpl::identity<load_standard>,
        // else
        BOOST_DEDUCED_TYPENAME mpl::eval_if<
            // no tracking
                    mpl::equal_to<
                        lslboost::serialization::tracking_level< T >,
                        mpl::int_<lslboost::serialization::track_never>
                >,
                // do a fast load
                mpl::identity<load_only>,
            // else
            // do a fast load only tracking is turned off
            mpl::identity<load_conditional>
        > > >::type typex;
        check_object_versioning< T >();
        check_object_level< T >();
        typex::invoke(ar, t);
    }
};

template<class Archive>
struct load_pointer_type {
    struct abstract
    {
        template<class T>
        static const basic_pointer_iserializer * register_type(Archive & /* ar */){
            // it has? to be polymorphic
            BOOST_STATIC_ASSERT(lslboost::is_polymorphic< T >::value);
            return static_cast<basic_pointer_iserializer *>(NULL);
         }
    };

    struct non_abstract
    {
        template<class T>
        static const basic_pointer_iserializer * register_type(Archive & ar){
            return ar.register_type(static_cast<T *>(NULL));
        }
    };

    template<class T>
    static const basic_pointer_iserializer * register_type(Archive &ar, const T & /*t*/){
        // there should never be any need to load an abstract polymorphic 
        // class pointer.  Inhibiting code generation for this
        // permits abstract base classes to be used - note: exception
        // virtual serialize functions used for plug-ins
        typedef BOOST_DEDUCED_TYPENAME
            mpl::eval_if<
                lslboost::serialization::is_abstract<const T>,
                lslboost::mpl::identity<abstract>,
                lslboost::mpl::identity<non_abstract>  
            >::type typex;
        return typex::template register_type< T >(ar);
    }

    template<class T>
    static T * pointer_tweak(
        const lslboost::serialization::extended_type_info & eti,
        void const * const t,
        const T &
    ) {
        // tweak the pointer back to the base class
        return static_cast<T *>(
            const_cast<void *>(
                lslboost::serialization::void_upcast(
                    eti,
                    lslboost::serialization::singleton<
                        BOOST_DEDUCED_TYPENAME 
                        lslboost::serialization::type_info_implementation< T >::type
                    >::get_const_instance(),
                    t
                )
            )
        );
    }

    template<class T>
    static void check_load(T & /* t */){
        check_pointer_level< T >();
        check_pointer_tracking< T >();
    }

    static const basic_pointer_iserializer *
    find(const lslboost::serialization::extended_type_info & type){
        return static_cast<const basic_pointer_iserializer *>(
            archive_serializer_map<Archive>::find(type)
        );
    }

    template<class Tptr>
    static void invoke(Archive & ar, Tptr & t){
        check_load(*t);
        const basic_pointer_iserializer * bpis_ptr = register_type(ar, *t);
        const basic_pointer_iserializer * newbpis_ptr = ar.load_pointer(
            // note major hack here !!!
            // I tried every way to convert Tptr &t (where Tptr might
            // include const) to void * &.  This is the only way
            // I could make it work. RR
            (void * & )t,
            bpis_ptr,
            find
        );
        // if the pointer isn't that of the base class
        if(newbpis_ptr != bpis_ptr){
            t = pointer_tweak(newbpis_ptr->get_eti(), t, *t);
        }
    }
};

template<class Archive>
struct load_enum_type {
    template<class T>
    static void invoke(Archive &ar, T &t){
        // convert integers to correct enum to load
        int i;
        ar >> lslboost::serialization::make_nvp(NULL, i);
        t = static_cast< T >(i);
    }
};

template<class Archive>
struct load_array_type {
    template<class T>
    static void invoke(Archive &ar, T &t){
        typedef BOOST_DEDUCED_TYPENAME remove_extent< T >::type value_type;
        
        // convert integers to correct enum to load
        // determine number of elements in the array. Consider the
        // fact that some machines will align elements on boundries
        // other than characters.
        std::size_t current_count = sizeof(t) / (
            static_cast<char *>(static_cast<void *>(&t[1])) 
            - static_cast<char *>(static_cast<void *>(&t[0]))
        );
        lslboost::serialization::collection_size_type count;
        ar >> BOOST_SERIALIZATION_NVP(count);
        if(static_cast<std::size_t>(count) > current_count)
            lslboost::serialization::throw_exception(
                archive::archive_exception(
                    lslboost::archive::archive_exception::array_size_too_short
                )
            );
        ar >> serialization::make_array(static_cast<value_type*>(&t[0]),count);
    }
};

} // detail

template<class Archive, class T>
inline void load(Archive & ar, T &t){
    // if this assertion trips. It means we're trying to load a
    // const object with a compiler that doesn't have correct
    // funtion template ordering.  On other compilers, this is
    // handled below.
    detail::check_const_loading< T >();
    typedef
        BOOST_DEDUCED_TYPENAME mpl::eval_if<is_pointer< T >,
            mpl::identity<detail::load_pointer_type<Archive> >
        ,//else
        BOOST_DEDUCED_TYPENAME mpl::eval_if<is_array< T >,
            mpl::identity<detail::load_array_type<Archive> >
        ,//else
        BOOST_DEDUCED_TYPENAME mpl::eval_if<is_enum< T >,
            mpl::identity<detail::load_enum_type<Archive> >
        ,//else
            mpl::identity<detail::load_non_pointer_type<Archive> >
        >
        >
        >::type typex;
    typex::invoke(ar, t);
}

#if 0

// BORLAND
#if BOOST_WORKAROUND(__BORLANDC__, BOOST_TESTED_AT(0x560))
// borland has a couple of problems
// a) if function is partially specialized - see below
// const paramters are transformed to non-const ones
// b) implementation of base_object can't be made to work
// correctly which results in all base_object s being const.
// So, strip off the const for borland.  This breaks the trap
// for loading const objects - but I see no alternative
template<class Archive, class T>
inline void load(Archive &ar, const T & t){
    load(ar, const_cast<T &>(t));
}
#endif

// let wrappers through.
#ifndef BOOST_NO_FUNCTION_TEMPLATE_ORDERING
template<class Archive, class T>
inline void load_wrapper(Archive &ar, const T&t, mpl::true_){
    lslboost::archive::load(ar, const_cast<T&>(t));
}

#if !BOOST_WORKAROUND(__BORLANDC__, BOOST_TESTED_AT(0x560))
template<class Archive, class T>
inline void load(Archive &ar, const T&t){
  load_wrapper(ar,t,serialization::is_wrapper< T >());
}
#endif 
#endif

#endif

} // namespace archive
} // namespace lslboost

#endif // BOOST_ARCHIVE_DETAIL_ISERIALIZER_HPP
