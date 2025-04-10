package com.sap.cloud.sdk.cloudplatform.connectivity;

import java.net.URI;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sap.cloud.sdk.cloudplatform.security.BasicCredentials;

import io.vavr.control.Option;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

public class TransparentProxyDestination implements HttpDestination
{

    @Delegate
    private final DestinationProperties baseProperties;

    @Nonnull
    final ImmutableList<Header> customHeaders;

    // the following 'cached' fields are ALWAYS derived from the baseProperties and stored in the corresponding fields
    // to avoid additional computation at runtime ONLY.
    // this is why we are calling them 'cached'.
    // since these values are ALWAYS derived from the provided baseProperties, we can safely assume that their values
    // are constant over the lifetime of this destination.
    // in other words: caching the values is safe and will not lead to any inconsistencies.
    // furthermore, it is safe to exclude these fields from the equals and hashCode methods because their values are
    // purely derived from the baseProperties, which are included in the equals and hashCode methods.
    @Nonnull
    private final Option<ProxyConfiguration> cachedProxyConfiguration;

    private TransparentProxyDestination(
        @Nonnull final DestinationProperties baseProperties,
        @Nullable final List<Header> customHeaders,
        @Nonnull final ComplexDestinationPropertyFactory destinationPropertyFactory )
    {
        this.baseProperties = baseProperties;
        this.customHeaders =
            customHeaders != null ? ImmutableList.<Header> builder().addAll(customHeaders).build() : ImmutableList.of();

        cachedProxyConfiguration = destinationPropertyFactory.getProxyConfiguration(baseProperties);

    }

    @Nonnull
    @Override
    public URI getUri()
    {
        return URI.create(baseProperties.get(DestinationProperty.URI).get());
    }

    @Nonnull
    @Override
    public Collection<Header> getHeaders( @Nonnull URI requestUri )
    {
        return customHeaders;
    }

    @Nonnull
    @Override
    public Option<String> getTlsVersion()
    {
        return get(DestinationProperty.TLS_VERSION);
    }

    @Nonnull
    @Override
    public Option<ProxyConfiguration> getProxyConfiguration()
    {
        return cachedProxyConfiguration;
    }

    @Nonnull
    @Override
    public Option<KeyStore> getKeyStore()
    {
        return null;
    }

    @Nonnull
    @Override
    public Option<String> getKeyStorePassword()
    {
        return null;
    }

    @Override
    public boolean isTrustingAllCertificates()
    {
        return false;
    }

    @Nonnull
    @Override
    public Option<BasicCredentials> getBasicCredentials()
    {
        return null;
    }

    @Nonnull
    @Override
    public AuthenticationType getAuthenticationType()
    {
        return null;
    }

    @Nonnull
    @Override
    public Option<ProxyType> getProxyType()
    {
        return Option.of(ProxyType.INTERNET);
    }

    @Nonnull
    @Override
    public Option<KeyStore> getTrustStore()
    {
        return null;
    }

    @Nonnull
    @Override
    public Option<String> getTrustStorePassword()
    {
        return null;
    }

    @Nonnull
    @Override
    public Option<Object> get( @Nonnull String key )
    {
        return null;
    }

    /**
     * Builder class to allow for easy creation of an immutable {@code DefaultHttpDestination} instance.
     */
    @Accessors( fluent = true, chain = true )
    public static class Builder
    {
        final List<Header> headers = Lists.newArrayList();

        final DefaultDestination.Builder builder = DefaultDestination.builder();

        final List<DestinationHeaderProvider> customHeaderProviders = new ArrayList<>();

        /**
         * Adds the given key-value pair to the destination to be created. This will overwrite any property already
         * assigned to the key.
         *
         * @param key
         *            The key to assign a property for.
         * @param value
         *            The property value to be assigned.
         * @return This builder.
         * @since 5.0.0
         */
        @Nonnull
        public Builder property( @Nonnull final String key, @Nonnull final Object value )
        {
            builder.property(key, value);
            return this;
        }

        /**
         * Adds the given key-value pair to the destination to be created. This will overwrite any property already
         * assigned to the key.
         *
         * @param key
         *            The {@link DestinationPropertyKey} to assign a property for.
         * @param value
         *            The property value to be assigned.
         * @param <ValueT>
         *            The type of the property value.
         * @return This builder.
         * @since 5.0.0
         */
        @Nonnull
        public <
            ValueT> Builder property( @Nonnull final DestinationPropertyKey<ValueT> key, @Nonnull final ValueT value )
        {
            return property(key.getKeyName(), value);
        }

        @Nonnull
        <ValueT> Option<ValueT> get( @Nonnull final DestinationPropertyKey<ValueT> key )
        {
            return builder.get(key);
        }

        @Nonnull
        <ValueT> Option<ValueT> get( @Nonnull final String key, @Nonnull final Function<Object, ValueT> conversion )
        {
            return builder.get(key, conversion);
        }

        /**
         * Removes the property with the given key from the destination to be created. This is useful when creating a
         * builder from an existing destination and wanting to remove a property.
         *
         * @param key
         *            The {@link DestinationPropertyKey} of the property to remove.
         * @return This builder.
         */
        @Nonnull
        public Builder removeProperty( @Nonnull final DestinationPropertyKey<?> key )
        {
            builder.removeProperty(key);
            return this;
        }

        /**
         * Removes the property with the given key from the destination to be created. This is useful when creating a
         * builder from an existing destination and wanting to remove a property.
         *
         * @param key
         *            The key of the property to remove.
         * @return This builder.
         */
        @Nonnull
        public Builder removeProperty( @Nonnull final String key )
        {
            builder.removeProperty(key);
            return this;
        }

        /**
         * Adds the given headers to the list of headers added to every outgoing request for this destination.
         *
         * @param headers
         *            Headers to add to outgoing requests.
         * @return This builder.
         */
        @Nonnull
        public Builder headers( @Nonnull final Collection<Header> headers )
        {
            this.headers.addAll(headers);
            return this;
        }

        /**
         * Adds the given header to the list of headers added to every outgoing request for this destination.
         *
         * @param header
         *            A header to add to outgoing requests.
         * @return This builder.
         */
        @Nonnull
        public Builder header( @Nonnull final Header header )
        {
            headers.add(header);
            return this;
        }

        /**
         * Adds a header given by the {@code headerName} and {@code headerValue} to the list of headers added to every
         * outgoing request for this destination.
         *
         * @param headerName
         *            The name of the header to add.
         * @param headerValue
         *            The value of the header to add.
         * @return This builder.
         */
        @Nonnull
        public Builder header( @Nonnull final String headerName, @Nonnull final String headerValue )
        {
            return header(new Header(headerName, headerValue));
        }

        @Nonnull
        public Builder destinationName( @Nonnull final String destinationName )
        {
            return header(new Header("X-Destination-Name", destinationName));
        }

        @Nonnull
        public Builder fragmentName( @Nonnull final String fragmentName )
        {
            return header(new Header("X-Fragment-Name", fragmentName));
        }

        @Nonnull
        public Builder tenantSubdomain( @Nonnull final String tenantSubdomain )
        {
            return header(new Header("X-Tenant-Subdomain", tenantSubdomain));
        }

        @Nonnull
        public Builder tenantId( @Nonnull final String tenantId )
        {
            return header(new Header("X-Tenant-Id", tenantId));
        }

        @Nonnull
        public Builder fragmentOptional( @Nonnull final String fragmentOptional )
        {
            return header(new Header("X-Fragment-Optional", fragmentOptional));
        }

        @Nonnull
        public Builder instanceName( @Nonnull final String instanceName )
        {

            this.property(DestinationProperty.URI, String.format("http://dynamic-%s.demohpa:80", instanceName));
            return this;
        }

        /**
         * Registers the provided {@link DestinationHeaderProvider} instances on this Destination.
         * <p>
         * For all outgoing requests, the registered header providers are invoked and the returned {@link Header
         * headers} are added to the request.
         *
         * @param headerProviders
         *            The header provider instances
         * @return This builder
         */
        @Nonnull
        public Builder headerProviders( @Nonnull final DestinationHeaderProvider... headerProviders )
        {
            customHeaderProviders.addAll(Arrays.asList(headerProviders));
            return this;
        }

        /**
         * Finally creates the {@code DefaultHttpDestination} with the properties retrieved via the
         * {@link #property(String, Object)} method.
         *
         * @return A fully instantiated {@code DefaultHttpDestination}.
         */
        @Nonnull
        public TransparentProxyDestination build()
        {
            if( !builder.get(DestinationProperty.URI).isDefined() ) {
                // DestinationProperty.URI is empty
                this.property(DestinationProperty.URI, "http://dynamic.demohpa:80");
            }
            return buildInternal();
        }

        TransparentProxyDestination buildInternal()
        {
            return new TransparentProxyDestination(builder.build(), headers, new ComplexDestinationPropertyFactory());
        }
    }
}
